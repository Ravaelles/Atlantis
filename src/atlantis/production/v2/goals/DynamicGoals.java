package atlantis.production.v2.goals;

import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnitType;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.TargetPlacement;
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
        /** Gas bank: Dragoons, tech and the second gas all read it. */
        public final int gas;
        public final int armySize;
        public final boolean inEarlyGame;

        /**
         * Advanced army unlocked (Protoss: Cybernetics Core standing; zeros
         * for other races until their policies fill equivalents). Gates the
         * Zealot-to-Dragoon advancement and the Singularity want.
         */
        public final boolean hasCore;
        /** Dragoons standing (Protoss; 0 elsewhere). Sizes the tech want. */
        public final int dragoons;
        /** Gas buildings standing (Protoss: Assimilators; 0 elsewhere). */
        public final int gasBuildings;
        /** Singularity Charge researched (Protoss; false elsewhere). */
        public final boolean singularity;
        /** Army strength for tech/economy gates. */
        public final int armyStrength;

        /** Cannon count already standing at the main base. */
        public final int cannonsAtMain;
        /** Enemy race facts the fortification policy reads. */
        public final boolean enemyIsZerg;
        public final boolean enemyIsProtoss;
        public final int enemyMutalisks;

        /** The base the fortification goals are anchored to; -1 when unknown. */
        public final int mainBaseTileX;
        public final int mainBaseTileY;

        /**
         * Supply providers (Pylons/Depots/Overlords) already ordered and not yet
         * finished. Counting them is what stops the supply goal being re-emitted
         * every frame until one completes.
         */
        public int supplyProvidersComing;

        /**
         * A Forge already standing (Protoss; other races leave it true so the
         * gate below stays a Protoss rule). Legacy never secured a base with
         * cannons before a Forge stood ({@code ProtossSecureBasesCommander}
         * gates on {@code Have.forge()}), and the V2 fortification port
         * dropped that gate - so a Cannon goal from frame 0 pulled an orphan
         * Forge ahead of the Gateway in openings whose build order has no
         * Forge (measured LIVE: Forge f2112-2746 before Gateway in three
         * Zealot-into-Goon games, zero Cannons ever completing). Set
         * post-construction like {@link #supplyProvidersComing}.
         */
        public boolean hasForge;

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
            this(workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame, armySize,
                basesExisting, cannonsAtMain, enemyIsZerg, enemyIsProtoss, enemyMutalisks,
                0, false, 0, 0, false, 0);
        }

        /**
         * The full snapshot plus tech/army inputs. The 13-argument form fills
         * zeros: a test that only cares about workers or supply keeps saying
         * exactly that, and tech goals stay silent on it.
         */
        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting,
                int cannonsAtMain, boolean enemyIsZerg, boolean enemyIsProtoss, int enemyMutalisks,
                int gas, boolean hasCore, int dragoons, int gasBuildings, boolean singularity,
                int armyStrength) {
            this(workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame, armySize,
                basesExisting, cannonsAtMain, enemyIsZerg, enemyIsProtoss, enemyMutalisks,
                gas, hasCore, dragoons, gasBuildings, singularity, armyStrength, -1, -1);
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
                gas, hasCore, dragoons, gasBuildings, singularity, armyStrength,
                tileX, tileY
            );
        }

        private GameSnapshot(
                int workers, int bases, int supplyUsed, int supplyFree,
                int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting,
                int cannonsAtMain, boolean enemyIsZerg, boolean enemyIsProtoss, int enemyMutalisks,
                int gas, boolean hasCore, int dragoons, int gasBuildings, boolean singularity,
                int armyStrength,
                int mainBaseTileX, int mainBaseTileY) {
            this.workers = workers;
            this.bases = bases;
            this.basesExisting = basesExisting;
            this.supplyUsed = supplyUsed;
            this.supplyFree = supplyFree;
            this.supplyTotal = supplyTotal;
            this.minerals = minerals;
            this.gas = gas;
            this.armySize = armySize;
            this.hasCore = hasCore;
            this.dragoons = dragoons;
            this.gasBuildings = gasBuildings;
            this.singularity = singularity;
            this.armyStrength = armyStrength;
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
    public static List<ProductionGoal> contribute(GameSnapshot state, ProductionRacePolicy race) {
        return contribute(state, race, false);
    }

    /**
     * @param buildOrderOwnsProvider the build order still holds an unproduced
     *                               provider row: the preventive supply fire
     *                               defers to it (emergency still fires).
     */
    public static List<ProductionGoal> contribute(
            GameSnapshot state, ProductionRacePolicy race, boolean buildOrderOwnsProvider) {
        List<ProductionGoal> goals = new ArrayList<>();

        addWorkerGoal(goals, state, race);
        addSupplyGoal(goals, state, race, buildOrderOwnsProvider);
        addArmyGoal(goals, state, race);
        addTechGoal(goals, state);
        addGasGoal(goals, state, race);
        addExpansionGoal(goals, state, race);
        addFortificationGoals(goals, state);

        goals.replaceAll(goal -> goal.withSource("dynamic"));

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
        // No Forge, no cannons: legacy secured bases with cannons only
        // once a Forge stood (ProtossSecureBasesCommander.applies gates on
        // Have.forge()). Without this a Cannon goal from frame 0 plans an
        // orphan Forge prerequisite that the dispatcher builds while the
        // Cannon itself never schedules - a Forge before the Gateway in
        // openings whose build order has no Forge.
        if (!state.hasForge) return;

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
    private static void addWorkerGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        AUnitType worker = race.workerType();
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
     *
     * <p>
     * While the build order still holds an unproduced provider row, the
     * PREVENTIVE fire defers to it: the row is the opening's single provider
     * author (measured: the supply goal warped the first Pylon at supply 6
     * for gate 8). The EMERGENCY fire (no free supply at all) never defers -
     * a live block is already a failed plan, and any author beats none.
     * </p>
     */
    public static ProductionGoal supplyGoal(GameSnapshot state, ProductionRacePolicy race) {
        return supplyGoal(state, race, false);
    }

    public static ProductionGoal supplyGoal(
            GameSnapshot state, ProductionRacePolicy race, boolean buildOrderOwnsProvider) {
        AUnitType supplyProvider = race.supplyProviderType();
        if (supplyProvider == null)
            return null;

        if (state.supplyFree > SUPPLY_WORRY_LEVEL)
            return null;

        if (buildOrderOwnsProvider && state.supplyFree > 0)
            return null;

        // A provider already on the way counts: supply arrives when the Pylon
        // finishes, so without this the goal is emitted every frame until then and
        // the scheduler orders a Pylon per frame (the "2800 ordered Pylons" run).
        // The same shape as the worker goal's workers-with-unfinished count.
        // Above 60 total supply two providers may fly at once (owner
        // 2026-10-19: production outruns warp completion there) - the second
        // coming is allowed, the third is not.
        if (state.supplyProvidersComing > (state.supplyTotal > 60 ? 1 : 0))
            return null;

        int priority = state.supplyFree <= 0
                ? ProductionGoal.PRIORITY_EMERGENCY
                : state.supplyUsed <= BuildOrderGoals.OPENING_SUPPLY_THRESHOLD
                    ? ProductionGoal.PRIORITY_OPENING_SUPPLY
                    : ProductionGoal.PRIORITY_DEPOTS;

        return new ProductionGoal(UnitProducible.of(supplyProvider), priority, 1, 0, TargetPlacement.anywhere());
    }

    /** Free supply below which a provider is wanted at all. */
    public static final int SUPPLY_WORRY_LEVEL = 4;

    private static void addSupplyGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        addSupplyGoal(goals, state, race, false);
    }

    private static void addSupplyGoal(
            List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race,
            boolean buildOrderOwnsProvider) {
        ProductionGoal goal = supplyGoal(state, race, buildOrderOwnsProvider);
        if (goal != null) goals.add(goal);
    }

    /**
     * Baseline army: always some combat units once we have a base and a
     * facility that can train them. The count is a floor, not a ceiling - the
     * strategic layer raises it through its own goals, and this generator exists
     * so the bot is never caught with an empty army while tech is researched.
     */
    private static void addArmyGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        // Below this the economy cannot support an army at all; the worker and
        // supply goals must win first.
        if (state.supplyUsed < 12)
            return;

        // Owner 2026-10-19: Dragoons ASAP. The moment the Core stands and
        // 50 gas is banked the advanced unit is wanted regardless of how
        // many Zealots stand - the old floor counted every army unit
        // against the transition and froze it forever behind a Zealot
        // army. Without gas for Dragoons, Zealots are a 275-mineral dump,
        // not a trickle - and pre-Core the opening Zealots flow ungated
        // (no mineral gate before the Core exists to save for).
        boolean wantGoons = state.hasCore && state.gas >= 50;
        AUnitType army;
        int have;
        if (wantGoons) {
            army = race.armyUnitTypeAdvanced();
            have = state.dragoons;
        }
        else {
            if (state.hasCore && state.minerals < 275)
                return;

            army = race.armyUnitType();
            have = state.armySize;
        }
        if (army == null)
            return;

        int targetArmy = Math.max(1, state.workers / 4);
        int missing = targetArmy - have;
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
     * Tech: Singularity Charge once Dragoons exist. Mirrors the legacy
     * trigger minus readiness (bank policy and in-flight dedup stay
     * execution-side: the wallet skips what the bank cannot cover, the
     * director refuses duplicate research). The Core itself is not named
     * here - the scheduler inserts it as a prerequisite from engine data.
     * Dragoons-gated, so other races stay silent without a race check.
     */
    private static void addTechGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (state.singularity)
            return;

        boolean want = state.dragoons >= 4
            || (state.gas >= 80 && (state.gas >= 180 || state.dragoons >= 6))
            || (state.dragoons >= 2 && state.supplyUsed >= 38
                && state.armyStrength >= 120 && state.minerals >= 240);
        if (!want)
            return;

        goals.add(new ProductionGoal(
                atlantis.production.v2.UpgradeProducible.of(bwapi.UpgradeType.Singularity_Charge),
                ProductionGoal.PRIORITY_NORMAL,
                1,
                0,
                TargetPlacement.anywhere()));
    }

    /**
     * Gas: the next refinery once two bases mine and the bank shapes like
     * the legacy commander's (supply, mineral-vs-gas, army and core gates).
     * The queue-cost clause is deliberately not mirrored: the scheduler's
     * in-flight dedup already refuses a second refinery while one is coming.
     * Placement is exact-geyser (the planner's gas branch), never a search.
     */
    private static void addGasGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        AUnitType gas = race.gasBuildingType();
        if (gas == null)
            return;

        if (state.supplyUsed < 40)
            return;

        if (state.bases < 2)
            return;

        if (!(state.gas < state.minerals && state.minerals >= 105))
            return;

        if (!(state.gas <= 150 || state.armySize >= 12))
            return;

        if (!(state.bases > state.gasBuildings))
            return;

        if (!(state.minerals >= 400 || state.hasCore))
            return;

        goals.add(new ProductionGoal(
                UnitProducible.of(gas),
                ProductionGoal.PRIORITY_GAS,
                1,
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
    private static void addExpansionGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        AUnitType base = race.baseType();
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

}
