package atlantis.production.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The scheduling engine: turns prioritized goals into a plan over a resource
 * timeline. Stateless across frames - every {@link #schedule} recomputes from
 * scratch; the only state is per pass.
 *
 * <p>
 * Invariants (pinned by {@code ProductionSchedulerTest}):
 * <ul>
 * <li>strict priority, stable order inside a priority;</li>
 * <li>an item never starts before its goal's {@code targetStartFrame}, its
 * prerequisites' availability, or a free slot on a concrete producer;</li>
 * <li>one facility holds one item at a time, and a goal never uses more than
 * its {@code producerLimit} facilities;</li>
 * <li>placement is validated before resources are allocated (a failed
 * placement leaves the timeline untouched);</li>
 * <li>supply providers add supply at completion; nothing is planned on supply
 * that does not exist yet;</li>
 * <li>a prerequisite shared by several goals is planned once; a malformed
 * recipe cycle is cut, not followed forever;</li>
 * <li>an item without any producer is unschedulable, never "free at frame 0".</li>
 * </ul>
 * </p>
 */
public final class ProductionScheduler {

    private final ProducerFacilityRegistry facilityRegistry;
    private final PlacementPlanner placementPlanner;
    private final ExistingItems existingItems;

    // ---- per-pass state, reset by schedule() ---------------------------------

    /** Facility id -> frame it frees up, including what this pass assigned. */
    private final Map<Integer, Integer> busyUntil = new HashMap<>();
    /** Facility id -> facility, for everything known this pass (existing + planned). */
    private final Map<String, List<ProducerFacility>> facilitiesByType = new HashMap<>();
    /** Item id -> earliest frame it becomes available through this plan. */
    private final Map<String, Integer> plannedAvailable = new HashMap<>();
    /** Facility ids consumed this pass (larvae). */
    private final Set<Integer> consumed = new HashSet<>();
    private int nextPlannedFacilityId;

    public ProductionScheduler(ProducerFacilityRegistry facilityRegistry, PlacementPlanner placementPlanner) {
        this(facilityRegistry, placementPlanner, ExistingItems.NONE);
    }

    public ProductionScheduler(ProducerFacilityRegistry facilityRegistry, PlacementPlanner placementPlanner,
            ExistingItems existingItems) {
        this.facilityRegistry = facilityRegistry;
        this.placementPlanner = placementPlanner;
        this.existingItems = existingItems != null ? existingItems : ExistingItems.NONE;
    }

    public ProductionPlan schedule(List<ProductionGoal> goals, ResourceTimeline timeline) {
        ProductionPlan plan = new ProductionPlan();
        busyUntil.clear();
        facilitiesByType.clear();
        plannedAvailable.clear();
        consumed.clear();
        nextPlannedFacilityId = -1;

        placementPlanner.startPass();

        List<ProductionGoal> sorted = new ArrayList<>(goals);
        Collections.sort(sorted); // stable: equal priorities keep list order

        for (ProductionGoal goal : sorted) {
            scheduleGoal(goal, timeline, plan);
        }

        placementPlanner.endPass();
        return plan;
    }

    // ---- goals ----------------------------------------------------------------

    private void scheduleGoal(ProductionGoal goal, ResourceTimeline timeline, ProductionPlan plan) {
        Producible item = goal.item();
        int earliest = Math.max(goal.targetStartFrame(), timeline.originFrame());

        int prerequisitesReady = ensurePrerequisites(item, earliest, timeline, plan, new HashSet<String>());
        if (prerequisitesReady < 0) return;

        // What the game already has, or already has coming, counts against the
        // goal BEFORE anything is planned.
        //
        // This is the fix for the plan that re-planned itself forever: the plan is
        // rebuilt every frame, and without this the scheduler placed a fresh item
        // every frame at the earliest affordable frame - which slides forward as the
        // minerals are spent, so the same Pylon was scheduled at 557, then 746, then
        // 974, and never built (measured 2026-10-08 on OpenBW). One order per thing
        // is a property of the SCHEDULER, not a rule every goal has to remember.
        int wanted = alreadyAvailableOrComing(item, goal);
        if (wanted <= 0) return;

        Set<Integer> usedProducers = new HashSet<>();

        for (int produced = 0; produced < wanted; produced++) {
            ProductionItem scheduled = scheduleItem(item, Math.max(earliest, prerequisitesReady), timeline, plan,
                    goal.placement(), false, goal.producerLimit(), usedProducers, goal.isContinuous());
            if (scheduled == null) return;
        }
    }

    /**
     * How many more of {@code goal}'s item are actually wanted, after subtracting
     * what exists and what is already on its way.
     *
     * <p>
     * A continuous goal ({@code count == -1}) wants one per free producer, so it is
     * never satisfied by a count - it is answered by the producer it will be given.
     * A counted goal wants {@code count}, satisfied by the per-type count the game
     * reports ({@code availableFrom} covers finished, under construction and
     * requested).
     * </p>
     */
    private int alreadyAvailableOrComing(Producible item, ProductionGoal goal) {
        if (goal.isContinuous()) return Integer.MAX_VALUE;

        // UNITS are covered by the goal generators themselves (the worker goal
        // counts workers existing PLUS in production, DynamicGoals.addWorkerGoal),
        // and they are produced from a facility queue that re-asks every frame by
        // design - so the scheduler must not subtract them here. Measured 2026-10-08:
        // doing so suppressed Probe production entirely once a fourth worker
        // existed (WorkerProductionTest.freshBasePlansWorkers).
        if (!item.requiresPlacement()) return goal.count();

        // A BUILDING is planned once and then exists on the map, so an item that is
        // already there or already requested needs no second plan. This is the fix
        // for the plan that re-planned itself forever: without it the scheduler
        // placed a fresh Pylon every frame at the earliest affordable frame - which
        // slides forward as the minerals are spent - so the same Pylon was scheduled
        // at 557, then 746, then 974, and never built (measured 2026-10-08 on
        // OpenBW). One order per building is a property of the SCHEDULER, not a rule
        // every goal has to remember.
        return existingItems.availableFrom(item) >= 0 ? 0 : goal.count();
    }

    /**
     * Makes sure every prerequisite of {@code item} is available or planned.
     * Returns the frame from which all of them are available, or -1 when one
     * of them cannot be had within the horizon.
     */
    private int ensurePrerequisites(Producible item, int notBefore, ResourceTimeline timeline,
            ProductionPlan plan, Set<String> path) {
        if (!path.add(item.id())) return -1; // recipe cycle: cut it

        int readyFrame = notBefore;
        for (Producible prerequisite : item.immediatePrerequisites()) {
            int available = availableFrom(prerequisite);
            if (available < 0) {
                int ownReady = ensurePrerequisites(prerequisite, timeline.originFrame(), timeline, plan, path);
                if (ownReady < 0) {
                    path.remove(item.id());
                    return -1;
                }
                ProductionItem planned = scheduleItem(prerequisite, ownReady, timeline, plan,
                        TargetPlacement.anywhere(), true, ProductionGoal.NO_PRODUCER_LIMIT,
                        new HashSet<Integer>(), false);
                if (planned == null) {
                    path.remove(item.id());
                    return -1;
                }
                available = planned.completionFrame();
            }
            readyFrame = Math.max(readyFrame, available);
        }

        path.remove(item.id());
        return readyFrame;
    }

    /** From the game or from this plan, whichever is earlier; -1 when neither. */
    private int availableFrom(Producible item) {
        int inGame = existingItems.availableFrom(item);
        Integer planned = plannedAvailable.get(item.id());
        if (inGame < 0) return planned == null ? -1 : planned;
        return planned == null ? inGame : Math.min(inGame, planned);
    }

    // ---- one item -------------------------------------------------------------

    private ProductionItem scheduleItem(Producible item, int notBefore, ResourceTimeline timeline,
            ProductionPlan plan, TargetPlacement placement, boolean isPrerequisite,
            int producerLimit, Set<Integer> usedProducers, boolean onlyFreeProducers) {
        ResourceCost cost = item.cost();

        ProducerFacility producer = null;
        int start;

        if (item.requiresPlacement()) {
            start = timeline.findEarliestAffordableFrame(cost, notBefore);
            if (start < 0) return null;
        } else {
            producer = chooseProducer(item, notBefore, producerLimit, usedProducers);
            if (producer == null) return null;

            start = timeline.findEarliestAffordableFrame(cost, Math.max(notBefore, freeFrom(producer)));
            if (start < 0) return null;

            // Continuous goals only fill producers that are free inside the
            // horizon; they never queue a second item behind the first.
            if (onlyFreeProducers && usedProducers.contains(producer.id())) return null;
        }

        // Placement BEFORE allocation: a failed reservation leaves the timeline as it was.
        PlacementReservation reservation = null;
        if (item.requiresPlacement()) {
            reservation = placementPlanner.reservePlacement(item, placement, start);
            if (reservation == null || !reservation.isSuccessful()) return null;
            if (reservation.readyFrame() > start) {
                start = timeline.findEarliestAffordableFrame(cost, reservation.readyFrame());
                if (start < 0) return null;
            }
        }

        timeline.allocate(cost, start);

        ProductionItem scheduled = new ProductionItem(item, start, isPrerequisite, reservation,
                producer != null && !producer.isPlanned() ? producer.id() : ProductionItem.NO_PRODUCER);
        plan.add(scheduled);
        commit(item, scheduled, producer, timeline, usedProducers);
        return scheduled;
    }

    private void commit(Producible item, ProductionItem scheduled, ProducerFacility producer,
            ResourceTimeline timeline, Set<Integer> usedProducers) {
        if (producer != null) {
            usedProducers.add(producer.id());
            if (item.consumesProducer()) consumed.add(producer.id());
            else busyUntil.put(producer.id(), scheduled.completionFrame());
        }

        if (item.supplyProvided() > 0) {
            timeline.addSupplyFrom(scheduled.completionFrame(), item.supplyProvided());
        }

        plannedAvailable.merge(item.id(), scheduled.completionFrame(), Math::min);

        if (item.becomesFacility()) {
            ProducerFacility planned = new ProducerFacility(nextPlannedFacilityId--, item.id(),
                    scheduled.completionFrame());
            facilities(item.id()).add(planned);
        }
    }

    // ---- producers ------------------------------------------------------------

    /**
     * The facility of the right type that frees up earliest (ties: registry
     * order). A goal at its producer limit only reuses facilities it already
     * holds.
     */
    private ProducerFacility chooseProducer(Producible item, int notBefore, int producerLimit,
            Set<Integer> usedProducers) {
        boolean atLimit = usedProducers.size() >= producerLimit;

        ProducerFacility best = null;
        int bestFrame = Integer.MAX_VALUE;
        for (ProducerFacility facility : facilities(item.producerTypeId())) {
            if (consumed.contains(facility.id())) continue;
            if (atLimit && !usedProducers.contains(facility.id())) continue;

            int frame = Math.max(notBefore, freeFrom(facility));
            if (frame < bestFrame) {
                bestFrame = frame;
                best = facility;
            }
        }
        return best;
    }

    private int freeFrom(ProducerFacility facility) {
        Integer busy = busyUntil.get(facility.id());
        return busy == null ? facility.availableFromFrame() : Math.max(busy, facility.availableFromFrame());
    }

    private List<ProducerFacility> facilities(String typeId) {
        List<ProducerFacility> known = facilitiesByType.get(typeId);
        if (known == null) {
            known = new ArrayList<>();
            if (typeId != null && facilityRegistry != null) {
                List<ProducerFacility> existing = facilityRegistry.facilitiesOf(typeId);
                if (existing != null) known.addAll(existing);
            }
            facilitiesByType.put(typeId, known);
        }
        return known;
    }
}
