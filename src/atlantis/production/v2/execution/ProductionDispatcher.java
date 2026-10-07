package atlantis.production.v2.execution;

import atlantis.production.v2.PlacementReservation;
import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Layer 4 of the production redesign (M4 of
 * _AI/redesign/01_PRODUCTION.md): turns a {@link ProductionPlan} into engine
 * commands, <b>only</b> for the items whose moment has come.
 *
 * <p>
 * Two time gates, both from Stardust's {@code issueOrders}:
 * <ul>
 * <li><b>Producing something</b> (training a unit, starting research) can be
 * issued up to {@code latencyFrames} early - the order is queued by the
 * engine and lands on the intended frame. Issuing later than the latency
 * window drops the command, not the frame, so early is correct.</li>
 * <li><b>Building something</b> is different: a builder must first walk to
 * the tile, and the walk is frames spent not producing. That work is only
 * started when the item is due <em>now</em> (or the plan says it is
 * already late), never because it will be due this frame plus latency.
 * In the legacy pipeline the same rule lived in
 * {@code ShouldNotTravelToConstructYet}.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Pure logic (DIP): it never touches BWAPI, {@code AUnit} or {@code Select} -
 * the director it holds is the only door to the engine. Everything here is
 * therefore unit-tested with a recording fake, which is what makes the
 * "execution timeliness" invariant (every committed item due inside the latency
 * window is issued exactly once) a provable statement and not a hope.
 * </p>
 *
 * <p>
 * Within one frame the items are issued in start-frame order. That is not a
 * detail: when funds only cover one of two due items, the one the schedule
 * ordered first is the one that gets the command.
 * </p>
 */
public final class ProductionDispatcher {

    private final OrderDirector director;

    public ProductionDispatcher(OrderDirector director) {
        this.director = director;
    }

    /**
     * Issues everything due at {@code currentFrame} and returns what was
     * issued, in order.
     */
    public List<DispatchResult> dispatch(ProductionPlan plan, int currentFrame, int latencyFrames) {
        List<ProductionItem> due = itemsDue(plan, currentFrame, latencyFrames);

        List<DispatchResult> issued = new ArrayList<>();
        for (ProductionItem item : due) {
            issued.add(issue(item));
        }
        return issued;
    }

    /**
     * The items of the plan whose command must leave now, ordered by start
     * frame. Visible for tests and for the dry-run comparison log.
     */
    public List<ProductionItem> itemsDue(ProductionPlan plan, int currentFrame, int latencyFrames) {
        List<ProductionItem> due = new ArrayList<>();
        for (ProductionItem item : plan.items()) {
            if (isDue(item, currentFrame, latencyFrames))
                due.add(item);
        }

        Collections.sort(due, new Comparator<ProductionItem>() {
            @Override
            public int compare(ProductionItem a, ProductionItem b) {
                return Integer.compare(a.startFrame(), b.startFrame());
            }
        });
        return due;
    }

    private boolean isDue(ProductionItem item, int currentFrame, int latencyFrames) {
        int earliestIssuable = item.startFrame() - latencyFrames;

        if (!item.item().requiresPlacement()) {
            return currentFrame >= earliestIssuable;
        }

        // A building in flight already has its builder; re-offering the tile is
        // how the seam stays idempotent, and it is the only way a builder that
        // died en route is replaced. The reservation reports the frame it was
        // committed on (-1 while the builder is still walking).
        PlacementReservation placement = item.placement();
        if (placement != null && placement.committedAtFrame() >= 0
                && placement.committedAtFrame() <= currentFrame) {
            return true;
        }

        return currentFrame >= item.startFrame();
    }

    private DispatchResult issue(ProductionItem item) {
        if (item.item().requiresPlacement()) {
            PlacementReservation placement = item.placement();
            if (placement == null || !placement.isSuccessful()) {
                return new DispatchResult(item, false, "no placement reservation");
            }
            boolean ok = director.buildAt(item.item(), placement);
            return new DispatchResult(item, ok, ok ? "builder committed" : "builder unavailable");
        }

        String producerTypeId = item.item().producerTypeId();
        if (producerTypeId == null || producerTypeId.equals("Unknown")) {
            return new DispatchResult(item, false, "no producer type");
        }

        boolean ok = director.trainFacility(producerTypeId, item.item());
        return new DispatchResult(item, ok, ok ? "trained" : "no free facility");
    }
}
