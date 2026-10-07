package atlantis.production.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The scheduling engine: turns prioritized goals into a plan over a resource
 * timeline. Stateless - every call recomputes from scratch and nothing is
 * cached between frames (the invariant that kills the legacy Queue's whole
 * class of bugs).
 *
 * <p>
 * Per goal, in strict priority order: insert missing prerequisites first,
 * then schedule the item itself at the earliest affordable frame. When the
 * horizon cannot afford something, the item is <b>skipped this pass</b> (it
 * will be recomputed next frame) - never dropped from the strategy, and never
 * produced on credit. Lower-priority goals schedule from the resources the
 * higher-priority ones did not claim, which is exactly the
 * "reserve-for-tech-first, macro-from-the-surplus" behaviour the redesign
 * asks for.
 * </p>
 *
 * <p>
 * Prerequisites are inserted recursively with their earliest affordable
 * frame computed <b>independently of the final item</b>: a Dragoon goal at
 * frame 300 does not pull the Cybernetics Core to frame 300 - the Core is
 * scheduled as early as the economy allows, and the Dragoon starts no earlier
 * than the Core completes. That ordering is pinned by the tests.
 * </p>
 */
public final class ProductionScheduler {

    private final ProducerFacilityRegistry facilityRegistry;
    private final PlacementPlanner placementPlanner;

    /** Planned buildings become facilities when they finish: type -> earliest available frame. */
    private final Map<String, Integer> plannedFacilityAvailableFrom = new HashMap<>();

    public ProductionScheduler(ProducerFacilityRegistry facilityRegistry, PlacementPlanner placementPlanner) {
        this.facilityRegistry = facilityRegistry;
        this.placementPlanner = placementPlanner;
    }

    public ProductionPlan schedule(List<ProductionGoal> goals, ResourceTimeline timeline) {
        ProductionPlan plan = new ProductionPlan();
        plannedFacilityAvailableFrom.clear();

        // One pass = one fresh plan: the planner must forget the tiles the
        // previous pass reserved, or the second Pylon of this frame would be
        // planned onto the first Pylon's spot.
        placementPlanner.startPass();

        List<ProductionGoal> sorted = new ArrayList<>(goals);
        Collections.sort(sorted);

        for (ProductionGoal goal : sorted) {
            scheduleGoal(goal, timeline, plan);
        }

        placementPlanner.endPass();
        return plan;
    }

    private void scheduleGoal(ProductionGoal goal, ResourceTimeline timeline, ProductionPlan plan) {
        Producible item = goal.item();

        // Prerequisites first, recursively.
        schedulePrerequisites(item, timeline, plan);

        for (int produced = 0; produced < countToProduce(goal); produced++) {
            if (!scheduleItem(item, timeline, plan, goal.placement(), false)) {
                return; // Cannot afford more within this pass - stop the goal.
            }
        }
    }

    private void schedulePrerequisites(Producible item, ResourceTimeline timeline, ProductionPlan plan) {
        for (Producible prerequisite : item.immediatePrerequisites()) {
            if (plan.contains(prerequisite))
                continue;

            // Recurse: a prerequisite can have prerequisites of its own.
            schedulePrerequisites(prerequisite, timeline, plan);
            scheduleItem(prerequisite, timeline, plan, TargetPlacement.anywhere(), true);
        }
    }

    /**
     * Schedules one item at the earliest affordable frame. Returns false when
     * the horizon cannot cover it (the goal gives up this pass).
     */
    private boolean scheduleItem(
            Producible item, ResourceTimeline timeline, ProductionPlan plan,
            TargetPlacement placement, boolean isPrerequisite) {
        // Earliest start = when the producer is free: existing facilities from
        // the registry, plus buildings planned earlier in this pass (the
        // Robotics Facility the prerequisite step just added is not in the
        // registry yet - Stardust's occupyUntil semantics; without this the
        // Reaver would schedule at frame 0 against a Robotics that only
        // finishes at 480).
        int earliestStart = earliestProducerFreeFrame(item);

        int affordableFrame = timeline.findEarliestAffordableFrame(item.cost(), earliestStart);
        if (affordableFrame < 0)
            return false;

        int startFrame = affordableFrame;

        PlacementReservation placementReservation = null;
        if (item.requiresPlacement()) {
            placementReservation = placementPlanner.reservePlacement(item, placement, startFrame);
            if (!placementReservation.isSuccessful())
                return false;
            startFrame = Math.max(startFrame, placementReservation.readyFrame());
        }

        timeline.allocate(item.cost(), startFrame);

        // A completed planned building becomes a facility of its own type from
        // its completion frame: the Robotics added as a prerequisite makes the
        // Reaver producible from 0+480, not from 0.
        if (item.requiresPlacement()) {
            plannedFacilityAvailableFrom.merge(item.id(), startFrame + item.buildDurationFrames(), Math::min);
        }

        plan.add(new ProductionItem(item, startFrame, isPrerequisite, placementReservation));

        return true;
    }

    private int earliestProducerFreeFrame(Producible item) {
        String typeId = item.producerTypeId();

        // Existing facilities: when this type can next start something. Empty
        // = the type does not exist yet (nothing in the registry, nothing
        // planned) - treated as "no facility at all", not as frame 0.
        int existingEarliest = Integer.MAX_VALUE;
        for (ProducerFacility facility : facilityRegistry.facilitiesOf(typeId)) {
            if (facility.availableFromFrame() < existingEarliest)
                existingEarliest = facility.availableFromFrame();
        }

        // Facilities planned earlier THIS pass (a Robotics built in this same
        // plan enables Reavers from its completion frame).
        Integer plannedAvailable = plannedFacilityAvailableFrom.get(typeId);

        if (existingEarliest == Integer.MAX_VALUE) {
            // No existing facility of this type: the planned one (if any) is
            // the only producer, otherwise the item is unproducible here.
            return plannedAvailable == null ? 0 : plannedAvailable;
        }

        return plannedAvailable == null ? existingEarliest : Math.min(existingEarliest, plannedAvailable);
    }

    private int countToProduce(ProductionGoal goal) {
        return goal.count() == ProductionGoal.COUNT_CONTINUOUS ? 1 : goal.count();
    }
}
