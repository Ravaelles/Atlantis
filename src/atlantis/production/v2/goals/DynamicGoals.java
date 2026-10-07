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
        public final int supplyUsed;
        public final int supplyFree;
        public final int supplyTotal;
        public final int minerals;
        public final boolean inEarlyGame;

        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                int supplyTotal, int minerals, boolean inEarlyGame) {
            this.workers = workers;
            this.bases = bases;
            this.supplyUsed = supplyUsed;
            this.supplyFree = supplyFree;
            this.supplyTotal = supplyTotal;
            this.minerals = minerals;
            this.inEarlyGame = inEarlyGame;
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

        return goals;
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
    private static void addSupplyGoal(List<ProductionGoal> goals, GameSnapshot state) {
        AUnitType supplyProvider = supplyProviderType();
        if (supplyProvider == null)
            return;

        if (state.supplyFree > 4)
            return;

        int priority = state.supplyFree <= 1
                ? ProductionGoal.PRIORITY_EMERGENCY
                : ProductionGoal.PRIORITY_DEPOTS;

        goals.add(new ProductionGoal(
                UnitProducible.of(supplyProvider),
                priority,
                1,
                0,
                TargetPlacement.anywhere()));
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

        goals.add(new ProductionGoal(
                UnitProducible.of(army),
                ProductionGoal.PRIORITY_MAINARMYBASE,
                targetArmy,
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
