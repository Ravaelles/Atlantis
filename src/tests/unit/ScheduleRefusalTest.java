package tests.unit;

import atlantis.production.v2.*;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The scheduler names WHY a wanted item never makes the plan (NEXT #202
 * family): an 18k-frame live stall (Cybernetics Core goal firing, first
 * plan 18k frames later) reads as a production stall instead of a siting
 * or bank problem. Transition-gated: the same item failing for the same
 * reason rows once, never per frame.
 */
@Tag("production")
public class ScheduleRefusalTest {

    @BeforeEach
    public void clearLogAndLatch() {
        EventLog.clearForTests();
        ScheduleRefusal.clearForTests();
    }

    @AfterEach
    public void clearLogAndLatchAgain() {
        EventLog.clearForTests();
        ScheduleRefusal.clearForTests();
    }

    @Test
    public void failedPlacementIsNamedOnce() {
        FakeProducible pylon = new FakeProducible("Pylon", true);
        FakePlanner failing = new FakePlanner(false);
        ResourceTimeline timeline = new ResourceTimeline(0, 2000, 500, 0, 8, 16);

        ProductionPlan empty = scheduler(failing).schedule(
            goals(pylon), timeline);
        assertTrue(empty.isEmpty(), "a refused tile plans nothing");

        ProductionPlan stillEmpty = scheduler(failing).schedule(
            goals(pylon), timeline);
        assertTrue(stillEmpty.isEmpty(), "still nothing on the re-fire");

        String rows = EventLog.toJsonLines();
        assertEquals(1, count(rows, "\"key\":\"SCHEDULE_REFUSED\""),
            "one row for the whole storm, not one per frame: " + rows);
        assertTrue(rows.contains("\"item\":\"Pylon\""), "names the item: " + rows);
        assertTrue(rows.contains("\"reason\":\"no-placement\""), "names the door: " + rows);
    }

    @Test
    public void missingProducerIsNamed() {
        FakeProducible dragoon = new FakeProducible("Dragoon", false);
        FakePlanner planner = new FakePlanner(true);
        // No Gateway in the registry: nobody can train this.
        ResourceTimeline timeline = new ResourceTimeline(0, 2000, 500, 200, 8, 16);

        ProductionPlan empty = new ProductionScheduler(
            new FakeRegistry(), planner, ExistingItems.NONE).schedule(
            goals(dragoon), timeline);
        assertTrue(empty.isEmpty(), "no facility plans nothing");

        String rows = EventLog.toJsonLines();
        assertTrue(rows.contains("\"reason\":\"no-free-producer\""), "names the door: " + rows);
    }

    private static int count(String haystack, String needle) {
        int n = 0;
        int at = -1;
        while ((at = haystack.indexOf(needle, at + 1)) >= 0) n++;

        return n;
    }

    private static List<ProductionGoal> goals(Producible item) {
        return Collections.singletonList(new ProductionGoal(
            item, ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    private static ProductionScheduler scheduler(FakePlanner planner) {
        return new ProductionScheduler(new FakeRegistry(), planner, ExistingItems.NONE);
    }

    private static final class FakeProducible implements Producible {
        private final String id;
        private final boolean building;

        FakeProducible(String id) {
            this(id, false);
        }

        FakeProducible(String id, boolean building) {
            this.id = id;
            this.building = building;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public ResourceCost cost() {
            return ResourceCost.of(100, 0, 0);
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
            return building ? null : "Gateway";
        }

        @Override
        public boolean requiresPlacement() {
            return building;
        }
    }

    private static final class FakeRegistry implements ProducerFacilityRegistry {
        private final Map<String, List<ProducerFacility>> byType = new HashMap<>();

        @Override
        public List<ProducerFacility> facilitiesOf(String typeId) {
            return byType.getOrDefault(typeId, new ArrayList<>());
        }
    }

    private static final class FakePlanner implements PlacementPlanner {
        private final boolean succeeds;

        FakePlanner(boolean succeeds) {
            this.succeeds = succeeds;
        }

        @Override
        public void startPass() {
        }

        @Override
        public PlacementReservation reservePlacement(
            Producible building, TargetPlacement constraint, int targetFrame) {
            if (!succeeds) return PlacementReservation.failure();

            return PlacementReservation.success(42, 18, targetFrame);
        }
    }
}
