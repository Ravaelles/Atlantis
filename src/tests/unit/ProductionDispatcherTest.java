package tests.unit;

import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.Producible;
import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.ResourceCost;
import atlantis.production.v2.TechProducible;
import atlantis.production.v2.UpgradeProducible;
import atlantis.production.v2.execution.DispatchResult;
import atlantis.production.v2.execution.OrderDirector;
import atlantis.production.v2.execution.ProductionDispatcher;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the execution-timeliness rules of the production-v2 dispatcher (M4 of
 * _AI/redesign/01_PRODUCTION.md). Pure logic: a recording {@link OrderDirector}
 * stands in for the engine, so the whole layer is provable without StarCraft.
 *
 * <p>
 * The two rules that matter, because getting either wrong produced the legacy
 * symptoms: producing something is issued up to {@code latencyFrames} early
 * (issuing late drops the command), while a builder is only sent to walk to a
 * construction site when the item is due now - never "because it will be due
 * within the latency window", which is how builders used to leave mining before
 * there was any building to build.
 * </p>
 */
public class ProductionDispatcherTest {

    private static final int LATENCY = 10;

    private static final class FakeProducible implements Producible {
        private final String id;
        private final boolean building;
        private final String producerTypeId;

        FakeProducible(String id, boolean building, String producerTypeId) {
            this.id = id;
            this.building = building;
            this.producerTypeId = producerTypeId;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public ResourceCost cost() {
            return ResourceCost.of(100, 0, 2);
        }

        @Override
        public int buildDurationFrames() {
            return 100;
        }

        @Override
        public List<Producible> immediatePrerequisites() {
            return new ArrayList<>();
        }

        @Override
        public String producerTypeId() {
            return producerTypeId;
        }

        @Override
        public boolean requiresPlacement() {
            return building;
        }
    }

    private static final class RecordingDirector implements OrderDirector {
        final List<String> commands = new ArrayList<>();
        boolean trainsSucceed = true;

        @Override
        public boolean trainFacility(String typeId, int producerId, Producible item) {
            commands.add("train:" + item.id() + "@" + typeId + "#" + producerId);
            return trainsSucceed;
        }

        @Override
        public boolean buildAt(Producible building, PlacementReservation placement) {
            commands.add("build:" + building.id() + "@" + placement.tileX() + "," + placement.tileY());
            return true;
        }

        @Override
        public boolean researchOrUpgrade(String typeId, int producerId, Producible item) {
            commands.add("research:" + item.id() + "@" + typeId + "#" + producerId);
            return true;
        }
    }

    private static ProductionPlan planOf(ProductionItem... items) {
        ProductionPlan plan = new ProductionPlan();
        for (ProductionItem item : items)
            plan.add(item);
        return plan;
    }

    @Test
    public void producingIsIssuedInsideTheLatencyWindowNotBeforeIt() {
        // A Zealot due at frame 100. With 10 frames of latency the command may
        // leave from frame 90; frame 89 is too early to bother (nothing is
        // gained, and the engine has no queue for "start in 10 frames").
        FakeProducible zealot = new FakeProducible("Zealot", false, "Gateway");
        ProductionPlan plan = planOf(new ProductionItem(zealot, 100, false));

        RecordingDirector director = new RecordingDirector();
        ProductionDispatcher dispatcher = new ProductionDispatcher(director);

        assertTrue(dispatcher.itemsDue(plan, 89, LATENCY).isEmpty(), "frame 89 is too early");
        assertEquals(1, dispatcher.itemsDue(plan, 90, LATENCY).size(), "frame 90 is the latency edge");
        assertEquals(1, dispatcher.itemsDue(plan, 100, LATENCY).size(), "and it stays due on the day");
    }

    @Test
    public void lateItemIsStillIssued() {
        // A due item must not be silently forgotten because the frame passed:
        // a plan computed after a stall still has to execute.
        FakeProducible zealot = new FakeProducible("Zealot", false, "Gateway");
        ProductionPlan plan = planOf(new ProductionItem(zealot, 100, false));

        RecordingDirector director = new RecordingDirector();
        List<DispatchResult> results = new ProductionDispatcher(director).dispatch(plan, 150, LATENCY);

        assertEquals(1, results.size());
        assertTrue(results.get(0).issued());
        assertEquals("train:Zealot@Gateway#0", director.commands.get(0));
    }

    @Test
    public void buildingIsNotIssuedBeforeItsFrameEvenInsideLatency() {
        // The rule that stops builders from leaving the mineral line early:
        // travel time is real production time, and the legacy pipeline only
        // committed a builder once the item was due.
        FakeProducible pylon = new FakeProducible("Pylon", true, "Probe");
        PlacementReservation placement = PlacementReservation.success(42, 18, 100);
        ProductionPlan plan = planOf(new ProductionItem(pylon, 100, false, placement));

        RecordingDirector director = new RecordingDirector();
        ProductionDispatcher dispatcher = new ProductionDispatcher(director);

        assertTrue(dispatcher.itemsDue(plan, 90, LATENCY).isEmpty(),
                "inside latency, a building is still not due");
        assertEquals(1, dispatcher.itemsDue(plan, 100, LATENCY).size());
    }

    @Test
    public void committedBuildingIsReofferedEveryFrameUntilItIsFinished() {
        // Idempotent re-commit: while the tile has a committed builder the
        // dispatcher keeps offering it, so a builder that dies en route is
        // replaced instead of the construction being orphaned.
        FakeProducible pylon = new FakeProducible("Pylon", true, "Probe");
        PlacementReservation committed = PlacementReservation.success(42, 18, 100).committedAt(100);
        ProductionPlan plan = planOf(new ProductionItem(pylon, 100, false, committed));

        RecordingDirector director = new RecordingDirector();
        ProductionDispatcher dispatcher = new ProductionDispatcher(director);

        assertEquals(1, dispatcher.itemsDue(plan, 140, LATENCY).size(),
                "an in-flight construction is still re-offered well after its start frame");
        assertTrue(dispatcher.itemsDue(plan, 90, LATENCY).isEmpty(),
                "but not before the tile was ever committed");
    }

    @Test
    public void failedTrainIsReportedNotSilentlySwallowed() {
        FakeProducible zealot = new FakeProducible("Zealot", false, "Gateway");
        ProductionPlan plan = planOf(new ProductionItem(zealot, 0, false));

        RecordingDirector director = new RecordingDirector();
        director.trainsSucceed = false;

        List<DispatchResult> results = new ProductionDispatcher(director).dispatch(plan, 0, LATENCY);

        assertEquals(1, results.size());
        assertFalse(results.get(0).issued(), "a command that did not leave is not 'issued'");
        assertEquals("no free facility", results.get(0).detail());
    }

    @Test
    public void itemsAreDispatchedInStartFrameOrder() {
        // When funds only pay for one of two due items, the schedule's order is
        // the order - not the order the plan happens to list them in.
        FakeProducible zealot = new FakeProducible("Zealot", false, "Gateway");
        FakeProducible dragoon = new FakeProducible("Dragoon", false, "Gateway");

        ProductionPlan plan = planOf(
                new ProductionItem(dragoon, 120, false),
                new ProductionItem(zealot, 100, false));

        RecordingDirector director = new RecordingDirector();
        new ProductionDispatcher(director).dispatch(plan, 200, LATENCY);

        assertEquals("train:Zealot@Gateway#0", director.commands.get(0));
        assertEquals("train:Dragoon@Gateway#0", director.commands.get(1));
    }

    @Test
    public void itemWithoutPlacementReservationIsNotDispatched() {
        // A building the planner failed to place must not be built at a
        // default tile; the pass was supposed to skip it entirely.
        FakeProducible pylon = new FakeProducible("Pylon", true, "Probe");
        ProductionPlan plan = planOf(new ProductionItem(pylon, 0, false, null));

        RecordingDirector director = new RecordingDirector();
        List<DispatchResult> results = new ProductionDispatcher(director).dispatch(plan, 0, LATENCY);

        assertEquals(1, results.size());
        assertFalse(results.get(0).issued());
        assertTrue(director.commands.isEmpty());
    }

    @Test
    public void theNamedProducerIsPassedToTheDirector() {
        // The plan names a facility (unit id): one Gateway must never take two
        // items for the same slot, and a research must go to its own facility.
        FakeProducible zealot = new FakeProducible("Zealot", false, "Gateway");
        ProductionPlan plan = planOf(new ProductionItem(zealot, 0, false, null, 138));

        RecordingDirector director = new RecordingDirector();
        new ProductionDispatcher(director).dispatch(plan, 0, LATENCY);

        assertEquals("train:Zealot@Gateway#138", director.commands.get(0));
    }

    @Test
    public void aTechGoesToResearchNotToTrain() {
        Producible charge = TechProducible.of(bwapi.TechType.Stim_Packs);
        ProductionPlan plan = planOf(new ProductionItem(charge, 0, false, null, 42));

        RecordingDirector director = new RecordingDirector();
        new ProductionDispatcher(director).dispatch(plan, 0, LATENCY);

        assertEquals("research:" + charge.id() + "@" + charge.producerTypeId() + "#42", director.commands.get(0),
                "a research goal sent to train() would silently do nothing");
    }

    @Test
    public void productionPlanItemsCannotBeMutatedByConsumers() {
        FakeProducible zealot = new FakeProducible("Zealot", false, "Gateway");
        ProductionPlan plan = planOf(new ProductionItem(zealot, 0, false));

        assertThrows(UnsupportedOperationException.class,
                () -> plan.items().clear(),
                "a consumer must not be able to invalidate the scheduler's result");
        assertEquals(1, plan.size());
    }

    @Test
    public void anUpgradeGoesToResearchToo() {
        Producible legs = UpgradeProducible.of(bwapi.UpgradeType.Leg_Enhancements);
        ProductionPlan plan = planOf(new ProductionItem(legs, 0, false, null, 7));

        RecordingDirector director = new RecordingDirector();
        new ProductionDispatcher(director).dispatch(plan, 0, LATENCY);

        assertTrue(director.commands.get(0).startsWith("research:" + legs.id() + "@"));
    }
}
