package tests.unit;

import atlantis.production.v2.PlacementPlanner;
import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.Producible;
import atlantis.production.v2.ProducerFacility;
import atlantis.production.v2.ProducerFacilityRegistry;
import atlantis.production.v2.ProductionGoal;
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

/**
 * Pins the pass semantics of the placement seam (M3 of
 * _AI/redesign/01_PRODUCTION.md): the planner is reset once per schedule pass,
 * and a reservation made earlier in a pass is visible to items planned later in
 * the same pass.
 *
 * <p>
 * This is the defect that motivates the seam having a lifecycle at all: without
 * it the position finder (which caches per builder/type/neighbourhood) hands the
 * same tile to every identical building of a frame - the duplicate-order class
 * the redesign exists to remove. The fake planner below models that by refusing
 * to hand out a tile it already handed out in the same pass.
 * </p>
 */
public class PlacementPassTest {

    private static final class FakeProducible implements Producible {
        private final String id;
        private final boolean building;

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
            return "Probe";
        }

        @Override
        public boolean requiresPlacement() {
            return building;
        }
    }

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

    /**
     * Hands out an incrementing tile per call and records the passes, so a test
     * can see both the per-pass reset and the duplicate suppression inside one
     * pass.
     */
    private static final class TilingPlanner implements PlacementPlanner {
        int passesStarted;
        int passesEnded;
        private int nextTile;
        final List<String> tilesTakenThisPass = new ArrayList<>();

        @Override
        public void startPass() {
            passesStarted++;
            nextTile = 0;
            tilesTakenThisPass.clear();
        }

        @Override
        public void endPass() {
            passesEnded++;
        }

        @Override
        public PlacementReservation reservePlacement(
                Producible building, TargetPlacement constraint, int targetFrame) {
            int x = nextTile++;
            String tile = x + ":7";
            if (tilesTakenThisPass.contains(tile)) return PlacementReservation.failure();
            tilesTakenThisPass.add(tile);
            return PlacementReservation.success(x, 7, targetFrame);
        }
    }

    @Test
    public void everySchedulePassStartsAndEndsThePlanner() {
        FakeRegistry registry = new FakeRegistry();
        registry.add("Probe", 0);

        TilingPlanner planner = new TilingPlanner();
        ProductionScheduler scheduler = new ProductionScheduler(registry, planner);

        scheduler.schedule(Collections.<ProductionGoal>emptyList(),
                new ResourceTimeline(100, 500, 0, 10));
        scheduler.schedule(Collections.<ProductionGoal>emptyList(),
                new ResourceTimeline(100, 500, 0, 10));

        assertEquals(2, planner.passesStarted, "one reset per pass");
        assertEquals(2, planner.passesEnded, "the pass is closed too");
    }

    @Test
    public void twoIdenticalBuildingsInOnePassGetDifferentTiles() {
        FakeRegistry registry = new FakeRegistry();
        registry.add("Probe", 0);

        FakeProducible pylon = new FakeProducible("Pylon", true);

        TilingPlanner planner = new TilingPlanner();
        ProductionScheduler scheduler = new ProductionScheduler(registry, planner);

        ProductionPlan plan = scheduler.schedule(Arrays.asList(
                new ProductionGoal(pylon, ProductionGoal.PRIORITY_DEPOTS, 2, 0, TargetPlacement.anywhere())),
                new ResourceTimeline(1000, 1000, 0, 40));

        long pylons = plan.items().stream().filter(i -> i.item().id().equals("Pylon")).count();
        assertEquals(2, pylons, "the goal asked for two Pylons");
        assertEquals(2, planner.tilesTakenThisPass.size(), "two tiles were reserved");
        assertEquals("0:7", planner.tilesTakenThisPass.get(0));
        assertEquals("1:7", planner.tilesTakenThisPass.get(1),
                "the second Pylon of the same pass must not reuse the first tile");
    }

    @Test
    public void reservationsOfOnePassDoNotLeakIntoTheNext() {
        FakeRegistry registry = new FakeRegistry();
        registry.add("Probe", 0);

        FakeProducible pylon = new FakeProducible("Pylon", true);

        TilingPlanner planner = new TilingPlanner();
        ProductionScheduler scheduler = new ProductionScheduler(registry, planner);

        List<ProductionGoal> goals = Arrays.asList(
                new ProductionGoal(pylon, ProductionGoal.PRIORITY_DEPOTS, 1, 0, TargetPlacement.anywhere()));

        scheduler.schedule(goals, new ResourceTimeline(1000, 1000, 0, 40));

        // Next pass: the plan is recomputed from scratch, so the same tile is
        // offered again rather than being permanently blacklisted.
        ProductionPlan second = scheduler.schedule(goals, new ResourceTimeline(1000, 1000, 0, 40));

        assertEquals(1, second.size(), "the next pass plans the Pylon again");
        assertEquals("0:7", planner.tilesTakenThisPass.get(0),
                "the tile of the previous pass is available again");
    }
}
