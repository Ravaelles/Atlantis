package atlantis.production.v2.goals;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ResourceCost;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.TargetPlacement;
import atlantis.production.v2.UnitProducible;
import atlantis.units.AUnitType;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.List;

/**
 * The two pull-forward heuristics and the supply emergency from
 * 01_PRODUCTION.md §"Producer::update()" (steps 3 and 4).
 *
 * <p>
 * The scheduler produces what the goals ask for, in priority order. These three
 * rules are what keep the plan from being merely correct: a plan that never
 * looks ahead on gas and supply is a plan that stalls the moment the economy
 * could have supported the next step.
 * </p>
 *
 * <ul>
 * <li><b>pullSupplyProviders</b> - a supply structure is planned
 * <em>before</em>
 * the block, not after it. A bot that reaches 200/200 supply with a Pylon
 * in the queue has already lost the frames the army needed.</li>
 * <li><b>pullRefineries</b> - gas structures come earlier when the mineral
 * bank can afford them, because every tech unit waits on gas.</li>
 * <li><b>emergency supply</b> - at the supply cap the provider jumps to the
 * emergency band: it is then the most important thing in the plan, ahead
 * of everything except defence.</li>
 * </ul>
 *
 * <p>
 * Pure logic over a {@link ResourceTimeline} and a small snapshot, so the rules
 * are unit-tested without a game: the scheduler asks "can we afford to pull
 * this
 * forward?" and the answer is arithmetic.
 * </p>
 */
public final class PullForwardGoals {

    /**
     * How many frames ahead the rules look. A few seconds of margin is enough to
     * hide one build duration and short enough that the mineral projection is
     * still meaningful.
     */
    private static final int LOOKAHEAD_FRAMES = 400;

    /** Free supply below which a provider is pulled forward pre-emptively. */
    private static final int SUPPLY_WORRY_LEVEL = 6;

    private PullForwardGoals() {
    }

    public static List<ProductionGoal> contribute(ResourceTimeline timeline, Snapshot state) {
        List<ProductionGoal> goals = new ArrayList<>();

        addSupplyProviderIfWorthPullingForward(goals, timeline, state);
        addGasIfMineralRich(goals, timeline, state);

        return goals;
    }

    /**
     * A supply provider is wanted while there is still room to build it; at the
     * cap it becomes an emergency (step 4 of the engine).
     */
    private static void addSupplyProviderIfWorthPullingForward(
            List<ProductionGoal> goals, ResourceTimeline timeline, Snapshot state) {
        AUnitType provider = supplyProvider();
        if (provider == null)
            return;
        if (state.supplyFree > SUPPLY_WORRY_LEVEL)
            return;

        int priority = state.supplyFree <= 0
                ? ProductionGoal.PRIORITY_EMERGENCY
                : ProductionGoal.PRIORITY_DEPOTS;

        // Only worth planning if the economy can pay within the lookahead; the
        // scheduler shifts it otherwise, which is the same outcome one pass later.
        ResourceCost cost = UnitProducible.of(provider).cost();
        int affordable = timeline.findEarliestAffordableFrame(cost, 0);
        if (affordable < 0 || affordable > LOOKAHEAD_FRAMES) return;

        goals.add(new ProductionGoal(UnitProducible.of(provider),
            priority, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Gas before it is needed: a mineral-rich economy with no refinery is one
     * that cannot spend its minerals on anything but workers.
     */
    private static void addGasIfMineralRich(
            List<ProductionGoal> goals, ResourceTimeline timeline, Snapshot state) {
        AUnitType refinery = gasBuilding();
        if (refinery == null)
            return;
        if (state.refineries >= state.desiredRefineries)
            return;
        if (state.minerals < RICH_MINERALS)
            return;

        ResourceCost cost = UnitProducible.of(refinery).cost();
        int affordable = timeline.findEarliestAffordableFrame(cost, 0);
        if (affordable < 0 || affordable > LOOKAHEAD_FRAMES)
            return;

        goals.add(new ProductionGoal(UnitProducible.of(refinery),
                ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    /** A mineral bank this large is not going to be spent on units alone. */
    private static final int RICH_MINERALS = 450;

    private static AUnitType supplyProvider() {
        if (We.protoss())
            return AUnitType.Protoss_Pylon;
        if (We.terran())
            return AUnitType.Terran_Supply_Depot;
        if (We.zerg())
            return AUnitType.Zerg_Overlord;
        return null;
    }

    private static AUnitType gasBuilding() {
        if (We.protoss())
            return AUnitType.Protoss_Assimilator;
        if (We.terran())
            return AUnitType.Terran_Refinery;
        if (We.zerg())
            return AUnitType.Zerg_Extractor;
        return null;
    }

    /** The few numbers these rules read, so they stay testable without a game. */
    public static final class Snapshot {
        public final int supplyFree;
        public final int minerals;
        public final int refineries;
        public final int desiredRefineries;

        public Snapshot(int supplyFree, int minerals, int refineries, int desiredRefineries) {
            this.supplyFree = supplyFree;
            this.minerals = minerals;
            this.refineries = refineries;
            this.desiredRefineries = desiredRefineries;
        }
    }
}
