package tests.unit;

import atlantis.production.v2.CommittedWork;
import atlantis.production.v2.EconomyModel;
import atlantis.production.v2.ExistingItems;
import atlantis.production.v2.PlacementPlanner;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.Producible;
import atlantis.production.v2.ProducerFacility;
import atlantis.production.v2.ProducerFacilityRegistry;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.ProductionScheduler;
import atlantis.production.v2.ResourceCost;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.TargetPlacement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The invariants of the scheduling engine (P1 of _AI/__01_PRODUCTION_TODO.md).
 *
 * <p>
 * Fake-backed on purpose: a hand-built {@link Producible}, a registry answered
 * from a map and a placement planner that can be told to fail. Every assertion
 * is therefore about the algorithm, not about a game.
 * </p>
 */
public class ProductionSchedulerTest {

    // ---- fakes -------------------------------------------------------------

    private static final class FakeProducible implements Producible {
        final String id;
        final ResourceCost cost;
        final int buildFrames;
        final int supplyProvided;
        final List<Producible> prerequisites;
        final String producerTypeId;
        final boolean isBuilding;
        final boolean becomesFacility;
        final boolean consumesProducer;

        FakeProducible(String id, int minerals, int gas, int supply, int buildFrames,
                List<Producible> prerequisites, String producerTypeId, boolean isBuilding) {
            this(id, minerals, gas, supply, buildFrames, prerequisites, producerTypeId, isBuilding,
                    isBuilding, false, 0);
        }

        FakeProducible(String id, int minerals, int gas, int supply, int buildFrames,
                List<Producible> prerequisites, String producerTypeId, boolean isBuilding,
                boolean becomesFacility, boolean consumesProducer, int supplyProvided) {
            this.id = id;
            this.cost = ResourceCost.of(minerals, gas, supply);
            this.buildFrames = buildFrames;
            this.prerequisites = prerequisites;
            this.producerTypeId = producerTypeId;
            this.isBuilding = isBuilding;
            this.becomesFacility = becomesFacility;
            this.consumesProducer = consumesProducer;
            this.supplyProvided = supplyProvided;
        }

        @Override public String id() { return id; }
        @Override public ResourceCost cost() { return cost; }
        @Override public int buildDurationFrames() { return buildFrames; }
        @Override public List<Producible> immediatePrerequisites() { return prerequisites; }
        @Override public String producerTypeId() { return producerTypeId; }
        @Override public boolean requiresPlacement() { return isBuilding; }
        @Override public boolean becomesFacility() { return becomesFacility; }
        @Override public boolean consumesProducer() { return consumesProducer; }
        @Override public int supplyProvided() { return supplyProvided; }
        @Override public String toString() { return id; }
    }

    private static final class FakeRegistry implements ProducerFacilityRegistry {
        private final Map<String, List<ProducerFacility>> byType = new HashMap<>();
        private int nextId = 1;

        FakeRegistry add(String typeId, int availableFromFrame) {
            byType.computeIfAbsent(typeId, k -> new ArrayList<>())
                    .add(new ProducerFacility(nextId++, typeId, availableFromFrame));
            return this;
        }

        @Override
        public List<ProducerFacility> facilitiesOf(String typeId) {
            return byType.getOrDefault(typeId, new ArrayList<>());
        }
    }

    private static class FakePlanner implements PlacementPlanner {
        boolean succeeds = true;
        int readyFrameDelta = 0;
        int reservations = 0;
        final List<String> reservedTiles = new ArrayList<>();
        int nextTile = 10;

        @Override
        public PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame) {
            reservations++;
            if (!succeeds) return PlacementReservation.failure();
            int tile = nextTile++;
            reservedTiles.add(building.id() + ":" + tile);
            return PlacementReservation.success(tile, 10, targetFrame + readyFrameDelta);
        }
    }

    private static final class FakeExisting implements ExistingItems {
        final Map<String, Integer> available = new HashMap<>();

        FakeExisting ready(Producible item) {
            available.put(item.id(), 0);
            return this;
        }

        FakeExisting availableAt(Producible item, int frame) {
            available.put(item.id(), frame);
            return this;
        }

        @Override
        public int availableFrom(Producible item) {
            Integer frame = available.get(item.id());
            return frame == null ? MISSING : frame;
        }
    }

    // ---- helpers -----------------------------------------------------------

    private static FakeProducible unit(String id, int minerals, int gas, int supply, int frames, String producer) {
        return new FakeProducible(id, minerals, gas, supply, frames, new ArrayList<>(), producer, false);
    }

    private static FakeProducible building(String id, int minerals, int gas, int frames, List<Producible> pre,
            String producer) {
        return new FakeProducible(id, minerals, gas, 0, frames, pre, producer, true);
    }

    private static ProductionScheduler scheduler(FakeRegistry registry, FakePlanner planner, ExistingItems existing) {
        return new ProductionScheduler(registry, planner, existing);
    }

    private static ProductionGoal goal(Producible item, int priority, int count) {
        return new ProductionGoal(item, priority, count, 0, TargetPlacement.anywhere());
    }

    private static int countOf(ProductionPlan plan, String id) {
        int total = 0;
        for (ProductionItem item : plan.items()) if (item.item().id().equals(id)) total++;
        return total;
    }

    // ---- priority and ordering --------------------------------------------

    @Test
    public void openingSchedulesOnePylonBeforeGatewayDespiteDuplicateSupplyGoal() {
        FakeProducible pylon = building("Pylon", 100, 0, 450, new ArrayList<>(), "Probe");
        FakeProducible gateway = building("Gateway", 150, 0, 600,
                Collections.<Producible>singletonList(pylon), "Probe");
        FakePlanner planner = new FakePlanner();
        ProductionGoal openingPylon = new ProductionGoal(pylon, ProductionGoal.PRIORITY_DEPOTS, 1, 0,
                TargetPlacement.anywhere());
        ProductionGoal gatewayRow = new ProductionGoal(gateway, ProductionGoal.PRIORITY_BASEDEFENSE, 1, 0,
                TargetPlacement.anywhere());
        ProductionGoal dynamicSupplyDuplicate = new ProductionGoal(pylon, ProductionGoal.PRIORITY_EMERGENCY, 1, 0,
                TargetPlacement.anywhere());

        ProductionPlan plan = scheduler(new FakeRegistry(), planner, ExistingItems.NONE).schedule(
                Arrays.asList(openingPylon, gatewayRow, dynamicSupplyDuplicate),
                new ResourceTimeline(0, 2000, 500, 0, 8, 16));

        assertEquals(1, countOf(plan, "Pylon"),
                "build-order and dynamic supply sources must not reserve the same Pylon twice: " + plan);
        assertEquals(1, countOf(plan, "Gateway"), "the opening must proceed to one Gateway: " + plan);
        assertEquals(2, planner.reservations,
                "the placement planner should reserve only the Pylon and Gateway, not the duplicate Pylon");
        assertTrue(plan.firstOf(gateway).startFrame() >= plan.firstOf(pylon).completionFrame(),
                "Gateway must remain gated by the first Pylon completing");
    }

    @Test
    public void higherPriorityGoalTakesTheEarlierFrame() {
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);
        FakeProducible first = unit("Zealot", 100, 0, 2, 240, "Gateway");
        FakeProducible second = unit("Dragoon", 125, 50, 2, 240, "Gateway");

        ResourceTimeline timeline = new ResourceTimeline(500, 0, 0, 10);
        timeline.addMiningIncome(0, 0.5, 0.2);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(Arrays.asList(
                new ProductionGoal(first, ProductionGoal.PRIORITY_EMERGENCY, 1, 0, TargetPlacement.anywhere()),
                new ProductionGoal(second, ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere())),
                timeline);

        assertTrue(plan.firstOf(first).startFrame() < plan.firstOf(second).startFrame());
    }

    @Test
    public void equalPriorityKeepsTheCallersOrder() {
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);
        FakeProducible a = unit("Zealot", 100, 0, 2, 240, "Gateway");
        FakeProducible b = unit("Dragoon", 125, 0, 2, 240, "Gateway");

        ResourceTimeline timeline = new ResourceTimeline(500, 0, 0, 10);
        timeline.addMiningIncome(0, 0.5, 0);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(Arrays.asList(
                goal(a, ProductionGoal.PRIORITY_NORMAL, 1), goal(b, ProductionGoal.PRIORITY_NORMAL, 1)), timeline);

        assertTrue(plan.firstOf(a).startFrame() < plan.firstOf(b).startFrame(),
                "same priority must keep the list order");
    }

    @Test
    public void targetStartFrameIsHonoredAndThenEarliestAffordable() {
        // 100 minerals, no income: affordable at 0. A goal targeting frame 50
        // must not start earlier, and one targeting 500 must give up.
        FakeProducible zealot = building("Pylon", 100, 0, 30, new ArrayList<>(), "Probe");

        ResourceTimeline timeline = new ResourceTimeline(600, 100, 0, 5);

        ProductionPlan plan = scheduler(new FakeRegistry(), new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(new ProductionGoal(zealot, 80, 1, 50, TargetPlacement.anywhere())),
                timeline);

        assertEquals(50, plan.firstOf(zealot).startFrame());

        ResourceTimeline tooLate = new ResourceTimeline(600, 100, 0, 5);
        ProductionPlan farFuture = scheduler(new FakeRegistry(), new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(new ProductionGoal(zealot, 80, 1, 900, TargetPlacement.anywhere())),
                tooLate);
        assertFalse(farFuture.contains(zealot), "a target beyond the horizon is not scheduled");
    }

    // ---- prerequisites ------------------------------------------------------

    @Test
    public void prerequisitesAreInsertedAndGateTheDependentItem() {
        FakeProducible core = building("Cybernetics Core", 150, 0, 250, new ArrayList<>(), "Probe");
        FakeProducible dragoon = new FakeProducible("Dragoon", 125, 50, 2, 300,
                Collections.singletonList(core), "Gateway", false);

        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(dragoon, 80, 1)),
                new ResourceTimeline(1200, 500, 200, 20));

        assertTrue(plan.contains(core), "the missing Core must be planned");
        assertTrue(plan.firstOf(dragoon).startFrame() >= plan.firstOf(core).completionFrame(),
                "a Dragoon cannot start before its Core completes");
    }

    @Test
    public void aPrerequisiteUnderConstructionIsNotPlannedAgainButGatesTheItem() {
        FakeProducible core = building("Cybernetics Core", 150, 0, 250, new ArrayList<>(), "Probe");
        FakeProducible dragoon = new FakeProducible("Dragoon", 125, 50, 2, 300,
                Collections.singletonList(core), "Gateway", false);

        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);
        FakeExisting existing = new FakeExisting().availableAt(core, 200);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), existing).schedule(
                Collections.singletonList(goal(dragoon, 80, 1)),
                new ResourceTimeline(1200, 500, 200, 20));

        assertFalse(plan.contains(core), "a Core being built must not be planned a second time");
        assertTrue(plan.firstOf(dragoon).startFrame() >= 200,
                "but the Dragoon still waits for it to finish");
    }

    @Test
    public void aCompletedFacilityInTheGameSatisfiesThePrerequisiteImmediately() {
        FakeProducible core = building("Cybernetics Core", 150, 0, 250, new ArrayList<>(), "Probe");
        FakeProducible dragoon = new FakeProducible("Dragoon", 125, 50, 2, 300,
                Collections.singletonList(core), "Gateway", false);

        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);
        FakeExisting existing = new FakeExisting().ready(core);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), existing).schedule(
                Collections.singletonList(goal(dragoon, 80, 1)),
                new ResourceTimeline(1200, 500, 200, 20));

        assertFalse(plan.contains(core));
        assertEquals(0, plan.firstOf(dragoon).startFrame(), "nothing to wait for");
    }

    @Test
    public void aSharedPrerequisiteIsPlannedOnlyOnce() {
        FakeProducible core = building("Cybernetics Core", 150, 0, 250, new ArrayList<>(), "Probe");
        FakeProducible dragoon1 = new FakeProducible("Dragoon", 125, 50, 2, 300,
                Collections.singletonList(core), "Gateway", false);
        FakeProducible dragoon2 = new FakeProducible("Dragoon", 125, 50, 2, 300,
                Collections.singletonList(core), "Gateway", false);

        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Arrays.asList(goal(dragoon1, 80, 1), goal(dragoon2, 80, 1)),
                new ResourceTimeline(2000, 1000, 500, 40));

        assertEquals(1, countOf(plan, "Cybernetics Core"), "one Core serves both goals");
        assertEquals(2, countOf(plan, "Dragoon"));
    }

    @Test
    public void aRecursiveRecipeIsCutInsteadOfLoopingForever() {
        // Malformed data: A requires B, B requires A.
        FakeProducible a = new FakeProducible("A", 100, 0, 0, 100, new ArrayList<>(), "Probe", true);
        FakeProducible b = new FakeProducible("B", 100, 0, 0, 100, Collections.<Producible>singletonList(a),
                "Probe", true);
        ((ArrayList<Producible>) a.prerequisites).add(b);

        ProductionPlan plan = scheduler(new FakeRegistry(), new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(a, 80, 1)), new ResourceTimeline(2000, 5000, 5000, 20));

        assertNotNull(plan, "the scheduler must survive a cycle");
        assertTrue(countOf(plan, "A") + countOf(plan, "B") <= 2);
    }

    // ---- producers ----------------------------------------------------------

    @Test
    public void oneGatewayNeverReceivesTwoItemsForTheSameSlot() {
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);
        FakeProducible zealot = unit("Zealot", 100, 0, 2, 240, "Gateway");

        // Huge bank: both could be paid at frame 0, but the single Gateway must
        // hold only one, the second starting when the first completes.
        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(zealot, 80, 2)), new ResourceTimeline(2000, 1000, 0, 20));

        List<ProductionItem> zealots = new ArrayList<>();
        for (ProductionItem item : plan.items()) if (item.item().id().equals("Zealot")) zealots.add(item);

        assertEquals(2, zealots.size());
        assertEquals(0, zealots.get(0).startFrame());
        assertEquals(zealots.get(0).completionFrame(), zealots.get(1).startFrame(),
                "the second Zealot waits for the Gateway to free up");
    }

    @Test
    public void twoGatewaysProduceInParallel() {
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0).add("Gateway", 0);
        FakeProducible zealot = unit("Zealot", 100, 0, 2, 240, "Gateway");

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(zealot, 80, 2)), new ResourceTimeline(2000, 1000, 0, 20));

        List<ProductionItem> zealots = new ArrayList<>();
        for (ProductionItem item : plan.items()) if (item.item().id().equals("Zealot")) zealots.add(item);

        assertEquals(zealots.get(0).startFrame(), zealots.get(1).startFrame(),
                "two Gateways start together, both at frame 0");
    }

    @Test
    public void producerLimitRestrictsHowManyFacilitiesAGoalUses() {
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0).add("Gateway", 0).add("Gateway", 0);
        FakeProducible zealot = unit("Zealot", 100, 0, 2, 240, "Gateway");

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(new ProductionGoal(zealot, 80, 3, 0, TargetPlacement.anywhere(), 1)),
                new ResourceTimeline(4000, 5000, 0, 40));

        int distinctProducers = 0;
        java.util.Set<Integer> producers = new java.util.HashSet<>();
        for (ProductionItem item : plan.items()) if (item.producerId() != ProductionItem.NO_PRODUCER) {
            producers.add(item.producerId());
        }
        distinctProducers = producers.size();

        assertEquals(1, distinctProducers, "a limit of one must use a single Gateway");
        assertEquals(3, countOf(plan, "Zealot"), "but still plan all three");
    }

    @Test
    public void anItemWithoutAnyProducerIsNotScheduled() {
        FakeProducible reaver = unit("Reaver", 200, 100, 4, 700, "Robotics Facility");

        ProductionPlan plan = scheduler(new FakeRegistry(), new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(reaver, 80, 1)), new ResourceTimeline(2000, 5000, 5000, 40));

        assertFalse(plan.contains(reaver), "no Robotics anywhere means no Reaver, not a frame-0 Reaver");
    }

    @Test
    public void aPlannedFacilitySuppliesProducersFromItsCompletionFrame() {
        FakeProducible robotics = building("Robotics Facility", 200, 100, 480, new ArrayList<>(), "Probe");
        FakeProducible reaver = new FakeProducible("Reaver", 200, 100, 4, 700,
                Collections.singletonList(robotics), "Robotics Facility", false);

        ProductionPlan plan = scheduler(new FakeRegistry(), new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(reaver, 80, 1)), new ResourceTimeline(3000, 5000, 5000, 40));

        assertTrue(plan.firstOf(reaver).startFrame() >= plan.firstOf(robotics).completionFrame(),
                "the Reaver waits for the Robotics this pass planned");
    }

    @Test
    public void aContinuousGoalFillsEachFreeProducerOnce() {
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0).add("Gateway", 0);
        FakeProducible zealot = unit("Zealot", 100, 0, 2, 240, "Gateway");

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(new ProductionGoal(zealot, 80, ProductionGoal.COUNT_CONTINUOUS, 0,
                        TargetPlacement.anywhere())),
                new ResourceTimeline(4000, 5000, 0, 40));

        assertEquals(2, countOf(plan, "Zealot"), "one item per free Gateway, not an unbounded queue");
    }

    @Test
    public void aConsumedProducerIsNotReused() {
        // A larva is used up by the unit it morphs into.
        FakeRegistry registry = new FakeRegistry().add("Larva", 0);
        FakeProducible zergling = new FakeProducible("Zergling", 50, 0, 1, 100, new ArrayList<>(), "Larva", false,
                false, true, 0);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(new ProductionGoal(zergling, 80, ProductionGoal.COUNT_CONTINUOUS, 0,
                        TargetPlacement.anywhere())),
                new ResourceTimeline(1000, 5000, 0, 40));

        assertEquals(1, countOf(plan, "Zergling"), "one larva produces one unit");
    }

    // ---- placement and resources -------------------------------------------

    @Test
    public void aFailedPlacementLeavesTheTimelineUntouched() {
        FakeProducible pylon = building("Pylon", 100, 0, 300, new ArrayList<>(), "Probe");
        FakeProducible gateway = building("Gateway", 150, 0, 500, new ArrayList<>(), "Probe");

        FakePlanner planner = new FakePlanner();
        planner.succeeds = false;

        ResourceTimeline timeline = new ResourceTimeline(2000, 150, 0, 20);

        ProductionPlan plan = scheduler(new FakeRegistry(), planner, ExistingItems.NONE).schedule(
                Collections.singletonList(goal(pylon, 80, 1)), timeline);

        assertTrue(plan.isEmpty(), "nothing was placed");
        assertTrue(timeline.canAffordAt(ResourceCost.of(150, 0, 0), 0),
                "the failed placement must not consume the minerals");
        assertEquals(150, timeline.mineralsAt(0));
    }

    @Test
    public void aFailedPlacementDoesNotBlockALaterValidItem() {
        FakeProducible pylon = building("Pylon", 100, 0, 300, new ArrayList<>(), "Probe");
        FakeProducible gateway = building("Gateway", 100, 0, 500, new ArrayList<>(), "Probe");

        FakePlanner planner = new FakePlanner() {
            @Override
            public PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int f) {
                if (building.id().equals("Pylon")) return PlacementReservation.failure();
                return super.reservePlacement(building, constraint, f);
            }
        };

        ResourceTimeline timeline = new ResourceTimeline(2000, 100, 0, 20);
        ProductionPlan plan = scheduler(new FakeRegistry(), planner, ExistingItems.NONE).schedule(
                Arrays.asList(goal(pylon, 80, 1), goal(gateway, 80, 1)), timeline);

        assertFalse(plan.contains(pylon));
        assertTrue(plan.contains(gateway), "the 100 minerals must still buy the Gateway");
        assertEquals(0, plan.firstOf(gateway).startFrame());
    }

    @Test
    public void aPlacementReadyLaterPushesTheStartForward() {
        FakeProducible pylon = building("Pylon", 100, 0, 300, new ArrayList<>(), "Probe");

        FakePlanner planner = new FakePlanner();
        planner.readyFrameDelta = 120; // builder travel time

        ProductionPlan plan = scheduler(new FakeRegistry(), planner, ExistingItems.NONE).schedule(
                Collections.singletonList(goal(pylon, 80, 1)), new ResourceTimeline(2000, 100, 0, 20));

        assertEquals(120, plan.firstOf(pylon).startFrame(),
                "the item starts when the builder can actually be there");
    }

    @Test
    public void twoBuildingsInOnePassNeverShareATile() {
        FakeProducible pylon = building("Pylon", 100, 0, 300, new ArrayList<>(), "Probe");
        FakePlanner planner = new FakePlanner();

        scheduler(new FakeRegistry(), planner, ExistingItems.NONE).schedule(
                Collections.singletonList(goal(pylon, 80, 2)), new ResourceTimeline(2000, 500, 0, 20));

        assertEquals(2, planner.reservedTiles.size());
        assertFalse(planner.reservedTiles.get(0).equals(planner.reservedTiles.get(1)));
    }

    @Test
    public void unaffordableItemIsSkippedNotScheduled() {
        FakeProducible zealot = unit("Zealot", 100, 0, 2, 100, "Gateway");
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(zealot, 80, 1)), new ResourceTimeline(20, 10, 0, 10));

        assertFalse(plan.contains(zealot));
    }

    @Test
    public void solvencyHoldsForEveryPlannedItem() {
        FakeProducible pylon = building("Pylon", 100, 0, 300, new ArrayList<>(), "Probe");
        FakeProducible gateway = building("Gateway", 150, 0, 500, Collections.<Producible>singletonList(pylon),
                "Probe");

        ResourceTimeline timeline = new ResourceTimeline(3000, 100, 0, 20);
        timeline.addMiningIncome(0, 0.3, 0);

        ProductionPlan plan = scheduler(new FakeRegistry(), new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(gateway, 80, 1)), timeline);

        assertTrue(plan.contains(pylon) && plan.contains(gateway));
        for (int frame = 0; frame < timeline.horizon(); frame++) {
            assertTrue(timeline.mineralsAt(frame) >= 0, "minerals went negative at " + frame);
            assertTrue(timeline.supplyAvailableAt(frame) >= 0, "supply went negative at " + frame);
        }
    }

    // ---- supply -------------------------------------------------------------

    @Test
    public void aPlannedProviderAddsSupplyAtItsCompletion() {
        FakeProducible pylon = new FakeProducible("Pylon", 100, 0, 0, 200, new ArrayList<>(), "Probe", true,
                true, false, 8);

        ResourceTimeline timeline = new ResourceTimeline(0, 2000, 100, 0, 4, 16);
        ProductionPlan plan = scheduler(new FakeRegistry(), new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(pylon, 80, 1)), timeline);

        assertEquals(4, timeline.supplyAvailableAt(0), "the Pylon is not built yet");
        assertEquals(12, timeline.supplyAvailableAt(plan.firstOf(pylon).completionFrame()),
                "from its completion frame the Pylon provides 8 supply");
    }

    @Test
    public void supplyGivenByAPlannedProviderIsUsableForLaterItems() {
        // Free supply 4 now; the Pylon adds 8 when it completes, so an item that
        // needs 12 must wait for it (or for the cap to rise) rather than be
        // rejected outright.
        FakeProducible pylon = new FakeProducible("Pylon", 100, 0, 0, 200, new ArrayList<>(), "Probe", true,
                true, false, 8);
        FakeProducible zealot = unit("Zealot", 100, 0, 12, 100, "Gateway");

        ResourceTimeline timeline = new ResourceTimeline(0, 2000, 300, 0, 4, 40);
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Arrays.asList(goal(pylon, 80, 1), goal(zealot, 80, 1)), timeline);

        assertTrue(plan.contains(zealot), "the 12-supply item is schedulable after the Pylon: " + plan);
        assertTrue(plan.firstOf(zealot).startFrame() >= plan.firstOf(pylon).completionFrame(),
                "the Zealot waits for the supply the Pylon will provide");
    }

    @Test
    public void anItemNeedingSupplyThatNeverArrivesIsSkipped() {
        // Free supply 4, total 40, item needs 12 and no provider is planned:
        // there is no frame at which it becomes affordable.
        FakeProducible zealot = unit("Zealot", 100, 0, 12, 100, "Gateway");
        FakeRegistry registry = new FakeRegistry().add("Gateway", 0);

        ProductionPlan plan = scheduler(registry, new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(zealot, 80, 1)), new ResourceTimeline(0, 2000, 5000, 0, 4, 40));

        assertFalse(plan.contains(zealot), "no supply, no unit - never on credit");
    }

    @Test
    public void totalSupplyIsCappedAtTheGameMaximum() {
        FakeProducible pylon = new FakeProducible("Pylon", 100, 0, 0, 200, new ArrayList<>(), "Probe", true,
                true, false, 8);

        ResourceTimeline timeline = new ResourceTimeline(0, 2000, 100, 0, 0, 197);
        scheduler(new FakeRegistry(), new FakePlanner(), ExistingItems.NONE).schedule(
                Collections.singletonList(goal(pylon, 80, 1)), timeline);

        assertEquals(ResourceTimeline.MAX_SUPPLY, timeline.supplyTotalAt(timeline.endFrame() - 1),
                "supply never exceeds 200");
        assertEquals(3, timeline.supplyAvailableAt(timeline.endFrame() - 1),
                "197 + 8 clamps to 200: a Pylon at the cap adds only 3 usable supply");
    }

    // ---- absolute frames ----------------------------------------------------

    @Test
    public void theTimelineWorksInAbsoluteFrames() {
        ResourceTimeline timeline = new ResourceTimeline(1000, 5000, 1000, 100, 0, 16);
        timeline.addMiningIncome(1000, 1.0, 0);

        // Income counts from the origin frame, so frame 1000 already holds one
        // frame of it. What matters is that a frame argument and a result are
        // both absolute game frames.
        assertEquals(1001, timeline.mineralsAt(1000), "the opening stock plus its frame of income");
        assertEquals(1101, timeline.mineralsAt(1100), "1000 + 101 frames of income");

        // 100 minerals held, 0 gas, and the income is minerals only: the gas
        // requirement is never met, so nothing is affordable.
        FakeProducible dragoon = unit("Dragoon", 100, 50, 2, 100, "Gateway");
        assertEquals(-1, timeline.findEarliestAffordableFrame(dragoon.cost(), 1100),
                "a result and an argument are the same clock (absolute frames)");
        assertEquals(1100, timeline.findEarliestAffordableFrame(ResourceCost.of(50, 0, 0), 1100),
                "and an affordable cost is found at the frame it is asked for");
    }

    @Test
    public void earliestAffordableFrameIsTheFirstAffordableOneNotTheLast() {
        // Regression: scanning the whole tail returned the LAST affordable frame,
        // so once income was non-zero every goal landed at the horizon.
        ResourceTimeline timeline = new ResourceTimeline(1000, 100, 0, 20);
        timeline.addMiningIncome(0, 0.5, 0);
        ResourceCost cost = ResourceCost.of(50, 0, 2);

        assertEquals(0, timeline.findEarliestAffordableFrame(cost, 0));
    }

    // ---- committed work (BWAPI accounting) ---------------------------------

    @Test
    public void anUnpaidConstructionReservesItsCostAtTheFrameItCanBePaid() {
        // A requested building whose builder is still walking has not been paid
        // yet: the cost must come off the timeline, at the frame income allows.
        ResourceTimeline timeline = new ResourceTimeline(0, 1000, 100, 0, 0, 20);
        timeline.addMiningIncome(0, 1.0, 0);

        int paidAt = CommittedWork.reserveUnpaid(timeline, ResourceCost.of(150, 0, 0));

        assertEquals(49, paidAt, "100 banked + one frame of income per frame: 150 is reached at frame 49");
        assertEquals(0, timeline.mineralsAt(49), "and the money is gone there");
        assertTrue(timeline.mineralsAt(48) <= 149, "not a frame earlier");
    }

    @Test
    public void anUnaffordableConstructionLeavesTheStocksAlone() {
        ResourceTimeline timeline = new ResourceTimeline(0, 100, 10, 0, 0, 20);

        assertEquals(-1, CommittedWork.reserveUnpaid(timeline, ResourceCost.of(150, 0, 0)));
        assertEquals(10, timeline.mineralsAt(0), "no credit: the stocks are untouched");
    }

    @Test
    public void aProviderUnderConstructionAddsItsSupplyAtCompletion() {
        ResourceTimeline timeline = new ResourceTimeline(0, 1000, 0, 0, 16, 16);

        CommittedWork.providerCompletesAt(timeline, 200, 8);

        assertEquals(16, timeline.supplyAvailableAt(199));
        assertEquals(24, timeline.supplyAvailableAt(200));
    }

    @Test
    public void aWorkerUnderConstructionStartsPayingAfterItsFirstTrip() {
        ResourceTimeline timeline = new ResourceTimeline(0, 2000, 0, 0, 0, 20);

        CommittedWork.workerCompletesAt(timeline, 100);

        int firstPaidFrame = 100 + EconomyModel.NEW_WORKER_FIRST_TRIP_FRAMES;
        assertEquals(0, timeline.mineralsAt(firstPaidFrame - 1),
                "a worker that just popped does not mine instantly");
        assertTrue(timeline.mineralsAt(firstPaidFrame + 100) > timeline.mineralsAt(firstPaidFrame));
    }
}
