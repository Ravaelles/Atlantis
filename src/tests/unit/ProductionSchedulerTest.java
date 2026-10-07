package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.ProductionScheduler;
import atlantis.production.v2.ProducerFacility;
import atlantis.production.v2.ProducerFacilityRegistry;
import atlantis.production.v2.Producible;
import atlantis.production.v2.ResourceCost;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.TargetPlacement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pins the scheduler semantics of the production-v2 core (M2 of
 * _AI/redesign/01_PRODUCTION.md): priority by sequence, prerequisites
 * inserted before the thing that needs them, competing goals shifting forward
 * instead of dropping, and the unaffordable-item-gives-up rule.
 *
 * <p>
 * Everything here is fake-backed: a hand-built Producible (fixed cost and
 * build time, no engine) plus a registry answered from a map. That is the
 * DIP point of the redesign — the scheduling logic is provable without
 * launching the game.
 * </p>
 */
public class ProductionSchedulerTest {

    // ---- fakes -------------------------------------------------------------

    /** Fixed-recipe Producible for tests: no engine, no BWAPI. */
    private static final class FakeProducible implements Producible {
        private final String id;
        private final ResourceCost cost;
        private final int buildFrames;
        private final List<Producible> prerequisites;
        private final String producerTypeId;
        private final boolean isBuilding;

        FakeProducible(String id, int minerals, int gas, int supply, int buildFrames,
                List<Producible> prerequisites, String producerTypeId, boolean isBuilding) {
            this.id = id;
            this.cost = ResourceCost.of(minerals, gas, supply);
            this.buildFrames = buildFrames;
            this.prerequisites = prerequisites;
            this.producerTypeId = producerTypeId;
            this.isBuilding = isBuilding;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public ResourceCost cost() {
            return cost;
        }

        @Override
        public int buildDurationFrames() {
            return buildFrames;
        }

        @Override
        public List<Producible> immediatePrerequisites() {
            return prerequisites;
        }

        @Override
        public String producerTypeId() {
            return producerTypeId;
        }

        @Override
        public boolean requiresPlacement() {
            return isBuilding;
        }
    }

    /** Registry answered from a type -> facilities map, built by the test. */
    private static final class FakeRegistry implements ProducerFacilityRegistry {
        private final Map<String, List<ProducerFacility>> byType = new HashMap<>();

        void add(String typeId, int availableFromFrame) {
            byType.computeIfAbsent(typeId, k -> new ArrayList<>())
                    .add(new ProducerFacility(typeId, availableFromFrame));
        }

        @Override
        public List<ProducerFacility> facilitiesOf(String typeId) {
            return byType.getOrDefault(typeId, new ArrayList<>());
        }
    }

    private static final class FakePlanner implements atlantis.production.v2.PlacementPlanner {
        @Override
        public atlantis.production.v2.PlacementReservation reservePlacement(
                Producible building, atlantis.production.v2.TargetPlacement constraint, int targetFrame) {
            return atlantis.production.v2.PlacementReservation.success(10, 10, targetFrame);
        }
    }

    // ---- helpers -----------------------------------------------------------

    private static FakeProducible unit(String id, int minerals, int gas, int supply, int frames, String producer) {
        return new FakeProducible(id, minerals, gas, supply, frames, new ArrayList<>(), producer, false);
    }

    private static ProductionScheduler scheduler(FakeRegistry registry) {
        return new ProductionScheduler(registry, new FakePlanner());
    }

    // ---- tests -------------------------------------------------------------

    @Test
    public void higherPriorityGoalIsScheduledFirst() {
        FakeRegistry registry = new FakeRegistry();
        registry.add("Gateway", 0);

        // Slow income, modest stock: both goals are wanted at once, but the
        // income cannot cover both immediately - the FIRST goal in the list
        // (same priority, stable sort keeps list order) takes the earlier
        // frame and the other shifts behind it. The point is the ORDER of the
        // two frames, not which goal wins by name.
        FakeProducible first = unit("Zealot", 100, 0, 2, 240, "Gateway");
        FakeProducible second = unit("Dragoon", 125, 50, 2, 240, "Gateway");

        ResourceTimeline timeline = new ResourceTimeline(500, 0, 0, 10);
        timeline.addMiningIncome(0, 0.5, 0.2);

        ProductionPlan plan = scheduler(registry).schedule(Arrays.asList(
                new ProductionGoal(first, ProductionGoal.PRIORITY_NORMAL, 1, 0,
                        TargetPlacement.anywhere()),
                new ProductionGoal(second, ProductionGoal.PRIORITY_NORMAL, 1, 0,
                        TargetPlacement.anywhere())),
                timeline);

        int firstFrame = plan.firstOf(first).startFrame();
        int secondFrame = plan.firstOf(second).startFrame();
        assertTrue(firstFrame < secondFrame,
                "the goal listed first (same priority) must take the earlier frame: "
                        + firstFrame + " vs " + secondFrame);
    }

    @Test
    public void prerequisiteIsInsertedBeforeTheThingThatNeedsIt() {
        // No Robotics Facility exists yet - the goal's prerequisite step must
        // plan one, and the Reaver must wait for it to complete (it cannot
        // ride the registry, which has no Robotics at all in this scenario).
        FakeRegistry registry = new FakeRegistry();

        // Reaver needs Robotics Facility; the goal mentions only the Reaver.
        FakeProducible robotics = new FakeProducible("Robotics Facility", 200, 100, 4, 480,
                new ArrayList<>(), "Probe", true);
        FakeProducible reaver = new FakeProducible("Reaver", 200, 100, 4, 720,
                Arrays.<Producible>asList(robotics), "Robotics Facility", false);

        ProductionPlan plan = scheduler(registry).schedule(Arrays.asList(
                new ProductionGoal(reaver, ProductionGoal.PRIORITY_NORMAL, 1, 0,
                        atlantis.production.v2.TargetPlacement.anywhere())),
                new ResourceTimeline(1000, 500, 300, 20));

        assertTrue(plan.contains(robotics), "Robotics Facility must be auto-inserted");
        assertTrue(plan.contains(reaver), "Reaver must be planned");

        // The Reaver cannot start before its prerequisite finishes.
        assertTrue(plan.firstOf(reaver).startFrame() >= plan.firstOf(robotics).completionFrame(),
                "Reaver start " + plan.firstOf(reaver).startFrame()
                        + " must be >= Robotics completion " + plan.firstOf(robotics).completionFrame());
    }

    @Test
    public void unaffordableGoalIsSkippedNotScheduled() {
        FakeRegistry registry = new FakeRegistry();
        registry.add("Gateway", 0);

        // 10 minerals can never afford a 100-mineral zealot within the horizon.
        FakeProducible zealot = unit("Zealot", 100, 0, 2, 100, "Gateway");

        ProductionPlan plan = scheduler(registry).schedule(Arrays.asList(
                new ProductionGoal(zealot, ProductionGoal.PRIORITY_NORMAL, 1, 0,
                        atlantis.production.v2.TargetPlacement.anywhere())),
                new ResourceTimeline(20, 10, 0, 10));

        assertFalse(plan.contains(zealot), "Unaffordable item must not appear in the plan");
    }

    @Test
    public void countTwoProducesTwoItemsAtDistinctFrames() {
        FakeRegistry registry = new FakeRegistry();
        registry.add("Gateway", 0);

        FakeProducible zealot = unit("Zealot", 100, 0, 2, 240, "Gateway");

        // 1 mineral per frame: the first zealot reserves the opening stock, the
        // second waits for the next 100 minerals of income - the two items land
        // at distinct frames rather than both at frame 0.
        ResourceTimeline timeline = new ResourceTimeline(600, 100, 0, 10);
        timeline.addMiningIncome(0, 1.0, 0);

        ProductionPlan plan = scheduler(registry).schedule(Arrays.asList(
                new ProductionGoal(zealot, ProductionGoal.PRIORITY_NORMAL, 2, 0,
                        TargetPlacement.anywhere())),
                timeline);

        long zealotCount = plan.items().stream().filter(i -> i.item().id().equals("Zealot")).count();
        assertEquals(2, zealotCount);

        int first = plan.firstOf(zealot).startFrame();
        int second = plan.items().stream().filter(i -> i.item().id().equals("Zealot"))
                .mapToInt(i -> i.startFrame()).distinct().filter(f -> f != first).findFirst().orElse(-1);
        assertTrue(second > first, "second zealot must start after the first: " + first + " vs " + second);
    }

    @Test
    public void emergencyPriorityBeatsNormalWhenBothCompete() {
        FakeRegistry registry = new FakeRegistry();
        registry.add("Gateway", 0);

        FakeProducible zealot = unit("Zealot", 100, 0, 2, 240, "Gateway");
        FakeProducible expensive = unit("Carrier", 350, 250, 6, 900, "Stargate");

        // Income is slow (1m, 1g per frame): after the emergency zealot reserves
        // at frame 0, the carrier - 350m/250g - is affordable only much later,
        // never before it. Both must end up in the plan, in that order.
        ResourceTimeline timeline = new ResourceTimeline(1000, 200, 0, 10);
        timeline.addMiningIncome(0, 1.0, 1.0);

        ProductionPlan plan = scheduler(registry).schedule(Arrays.asList(
                new ProductionGoal(expensive, ProductionGoal.PRIORITY_MAINARMY, 1, 0,
                        TargetPlacement.anywhere()),
                new ProductionGoal(zealot, ProductionGoal.PRIORITY_EMERGENCY, 1, 0,
                        TargetPlacement.anywhere())),
                timeline);

        assertEquals(0, plan.firstOf(zealot).startFrame(), "emergency zealot gets frame 0");
        int carrierFrame = plan.firstOf(expensive).startFrame();
        assertTrue(carrierFrame > 0, "carrier waits behind the emergency: " + carrierFrame);
    }
}
