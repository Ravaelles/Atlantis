package atlantis.production.v2.goals;

import atlantis.config.AtlantisRaceConfig;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnitType;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.List;

/**
 * The goal generators that replace the legacy dynamic commanders (M5 of
 * _AI/redesign/01_PRODUCTION.md).
 *
 * <p>
 * The old pipeline had one commander per concern - {@code
 * AutoProduceWorkersCommander}, {@code DynamicBuildingsCommander},
 * {@code ExpansionCommander}, {@code SupplyCommander} - and each of them
 * <em>issued orders</em> directly, racing the static build order and each other
 * through a mutable queue. Here the same concerns only ever <em>declare what
 * they want</em>: a goal is a value, it carries a priority band, and the single
 * scheduler decides what the economy can actually pay for. That separation is
 * the whole change: policy states intent, the engine resolves contention.
 * </p>
 *
 * <p>
 * Every generator is a pure function of a small {@link GameSnapshot} - a
 * handful of numbers, not the game - so the whole policy layer is unit-tested
 * without StarCraft. The live snapshot is assembled by the v2 commander.
 * </p>
 */
public final class DynamicGoals {

    /**
     * The few facts a goal decision needs. Deliberately primitive: this is the
     * seam that keeps the goal layer testable, and it is intentionally not a
     * window onto {@code Select}/{@code Count} - if a decision needs more than
     * these numbers, it does not belong in this layer.
     */
    public static final class GameSnapshot {
        public final int workers;
        public final int bases;
        /** Bases finished, under construction and pending - what counts as "we have one". */
        public final int basesExisting;
        public final int supplyUsed;
        public final int supplyFree;
        public final int supplyTotal;
        public final int minerals;
        public final int armySize;
        public final boolean inEarlyGame;

        /** Cannon count already standing at the main base. */
        public final int cannonsAtMain;
        /** Enemy race facts the fortification policy reads. */
        public final boolean enemyIsZerg;
        public final boolean enemyIsProtoss;
        public final int enemyMutalisks;

        /** The base the fortification goals are anchored to; -1 when unknown. */
        public final int mainBaseTileX;
        public final int mainBaseTileY;

        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                int supplyTotal, int minerals, boolean inEarlyGame) {
            this(workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame, 0, bases);
        }

        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting) {
            this(workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame, armySize,
                basesExisting, 0, false, false, 0);
        }

        /**
         * The full snapshot, including the fortification inputs. Kept as one
         * constructor rather than a builder: the goal layer is deliberately fed a
         * handful of numbers, and a test that needs to say "zerg, twelve mutas"
         * should be able to say exactly that.
         */
        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting,
                int cannonsAtMain, boolean enemyIsZerg, boolean enemyIsProtoss, int enemyMutalisks) {
            this.workers = workers;
            this.bases = bases;
            this.basesExisting = basesExisting;
            this.supplyUsed = supplyUsed;
            this.supplyFree = supplyFree;
            this.supplyTotal = supplyTotal;
            this.minerals = minerals;
            this.armySize = armySize;
            this.inEarlyGame = inEarlyGame;
            this.cannonsAtMain = cannonsAtMain;
            this.enemyIsZerg = enemyIsZerg;
            this.enemyIsProtoss = enemyIsProtoss;
            this.enemyMutalisks = enemyMutalisks;
            this.mainBaseTileX = -1;
            this.mainBaseTileY = -1;
        }

        /**
         * The same snapshot anchored to a base, for the fortification policy. The
         * 13-argument form leaves the anchor unknown, which is what a test that only
         * cares about workers or supply wants - and what makes fortification emit
         * nothing rather than guess a location.
         */
        public GameSnapshot atBase(int tileX, int tileY) {
            return new GameSnapshot(
                workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame,
                armySize, basesExisting, cannonsAtMain, enemyIsZerg, enemyIsProtoss, enemyMutalisks,
                tileX, tileY
            );
        }

        private GameSnapshot(
                int workers, int bases, int supplyUsed, int supplyFree,
                int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting,
                int cannonsAtMain, boolean enemyIsZerg, boolean enemyIsProtoss, int enemyMutalisks,
                int mainBaseTileX, int mainBaseTileY) {
            this.workers = workers;
            this.bases = bases;
            this.basesExisting = basesExisting;
            this.supplyUsed = supplyUsed;
            this.supplyFree = supplyFree;
            this.supplyTotal = supplyTotal;
            this.minerals = minerals;
            this.armySize = armySize;
            this.inEarlyGame = inEarlyGame;
            this.cannonsAtMain = cannonsAtMain;
            this.enemyIsZerg = enemyIsZerg;
            this.enemyIsProtoss = enemyIsProtoss;
            this.enemyMutalisks = enemyMutalisks;
            this.mainBaseTileX = mainBaseTileX;
            this.mainBaseTileY = mainBaseTileY;
        }
    }

    /** Workers target per base before the generator stops asking for more. */
    private static final int WORKERS_PER_BASE = 25;
    private static final int ABSOLUTE_WORKER_CAP = 80;

    private DynamicGoals() {
    }

    /**
     * All dynamic goals for this frame, priority-ordered by the caller's sort.
     * Empty when the snapshot cannot support any of them.
     */
    public static List<ProductionGoal> contribute(GameSnapshot state) {
        List<ProductionGoal> goals = new ArrayList<>();

        addWorkerGoal(goals, state);
        addSupplyGoal(goals, state);
        addArmyGoal(goals, state);
        addExpansionGoal(goals, state);
        addFortificationGoals(goals, state);

        return goals;
    }

    /**
     * Base fortification (Protoss): the cannon policy from
     * {@code atlantis.placement.policy} expressed as ordinary goals.
     *
     * <p>
     * This is where the placement rewrite and Production V2 meet, and the join is
     * deliberately one-directional: the policy says "this base wants N cannons near
     * here" and the planner resolves the constraint to a choke-aware tile (S4). No
     * tile is computed here, which is the §5.3 boundary of
     * {@code _AI/redesign/03_PLACEMENT.md} - policy emits intent, placement decides
     * where.
     * </p>
     */
    private static void addFortificationGoals(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (state.mainBaseTileX < 0) return;

        atlantis.placement.policy.CannonFortificationPolicy.Context context =
            new atlantis.placement.policy.CannonFortificationPolicy.Context(
                state.supplyTotal,
                state.minerals,
                state.cannonsAtMain,
                state.enemyIsZerg,
                state.enemyIsProtoss,
                state.enemyMutalisks
            );

        goals.addAll(atlantis.placement.policy.FortificationGoals.forBase(
            context, UnitProducible.of(AUnitType.Protoss_Photon_Cannon),
            state.mainBaseTileX, state.mainBaseTileY
        ));
    }

    /**
     * Continuous worker production, up to the saturation of the bases we have.
     * The old commander computed this per frame with a long list of vetoes; the
     * v2 form is the saturation rule itself, because the vetoes were really
     * about "is there money for anything more important" - and that question is
     * now answered once, by the timeline, for every goal at the same time.
     */
    private static void addWorkerGoal(List<ProductionGoal> goals, GameSnapshot state) {
        AUnitType worker = workerType();
        if (worker == null)
            return;

        if (state.workers >= ABSOLUTE_WORKER_CAP)
            return;
        if (state.workers >= WORKERS_PER_BASE * Math.max(1, state.bases))
            return;

        goals.add(new ProductionGoal(
                UnitProducible.of(worker),
                ProductionGoal.PRIORITY_WORKERS,
                1,
                0,
                TargetPlacement.anywhere()));
    }

    /**
     * Supply-block prevention: a Pylon/Depot/Overlord the moment free supply
     * runs low. It is emitted as an emergency only at the edge (so it never
     * outranks real defence early), otherwise it is a normal-priority goal.
     */
    public static ProductionGoal supplyGoal(GameSnapshot state) {
        AUnitType supplyProvider = supplyProviderType();
        if (supplyProvider == null)
            return null;

        if (state.supplyFree > SUPPLY_WORRY_LEVEL)
            return null;

        int priority = state.supplyFree <= 0
                ? ProductionGoal.PRIORITY_EMERGENCY
                : ProductionGoal.PRIORITY_DEPOTS;

        return new ProductionGoal(UnitProducible.of(supplyProvider), priority, 1, 0, TargetPlacement.anywhere());
    }

    /** Free supply below which a provider is wanted at all. */
    public static final int SUPPLY_WORRY_LEVEL = 4;

    private static void addSupplyGoal(List<ProductionGoal> goals, GameSnapshot state) {
        ProductionGoal goal = supplyGoal(state);
        if (goal != null) goals.add(goal);
    }

    /**
     * Baseline army: always some combat units once we have a base and a
     * facility that can train them. The count is a floor, not a ceiling - the
     * strategic layer raises it through its own goals, and this generator exists
     * so the bot is never caught with an empty army while tech is researched.
     */
    private static void addArmyGoal(List<ProductionGoal> goals, GameSnapshot state) {
        AUnitType army = armyType();
        if (army == null)
            return;

        // Below this the economy cannot support an army at all; the worker and
        // supply goals must win first.
        if (state.supplyUsed < 12)
            return;

        int targetArmy = Math.max(1, state.workers / 4);
        int missing = targetArmy - state.armySize;
        if (missing <= 0)
            return;

        goals.add(new ProductionGoal(
                UnitProducible.of(army),
                ProductionGoal.PRIORITY_MAINARMYBASE,
                missing,
                0,
                TargetPlacement.anywhere()));
    }

    /**
     * Expansion: one more base is wanted as soon as the current one is close to
     * saturated. Expressed as a goal, so the timeline decides whether the 400
     * minerals belong to the expansion or to the defence this frame - the old
     * commander decided that by issuing the order first and letting the queue
     * sort it out.
     */
    private static void addExpansionGoal(List<ProductionGoal> goals, GameSnapshot state) {
        AUnitType base = baseType();
        if (base == null)
            return;

        if (state.workers < WORKERS_PER_BASE * state.bases)
            return;
        if (state.bases >= 4)
            return;

        // A base already under construction is not missing: without this the
        // generator asks for a second Nexus every frame while the first is
        // being built.
        if (state.basesExisting > state.bases)
            return;

        goals.add(new ProductionGoal(
                UnitProducible.of(base),
                ProductionGoal.PRIORITY_NORMAL,
                1,
                0,
                TargetPlacement.anywhere()));
    }

    private static AUnitType workerType() {
        if (We.protoss())
            return AUnitType.Protoss_Probe;
        if (We.terran())
            return AUnitType.Terran_SCV;
        if (We.zerg())
            return AUnitType.Zerg_Drone;
        return AtlantisRaceConfig.WORKER;
    }

    private static AUnitType armyType() {
        if (We.protoss())
            return AUnitType.Protoss_Zealot;
        if (We.terran())
            return AUnitType.Terran_Marine;
        if (We.zerg())
            return AUnitType.Zerg_Zergling;
        return null;
    }

    private static AUnitType baseType() {
        if (We.protoss())
            return AUnitType.Protoss_Nexus;
        if (We.terran())
            return AUnitType.Terran_Command_Center;
        if (We.zerg())
            return AUnitType.Zerg_Hatchery;
        return null;
    }

    private static AUnitType supplyProviderType() {
        if (We.protoss())
            return AUnitType.Protoss_Pylon;
        if (We.terran())
            return AUnitType.Terran_Supply_Depot;
        if (We.zerg())
            return AUnitType.Zerg_Overlord;
        return null;
    }
}
