package atlantis.production.placement;
import atlantis.production.producibles.Producible;

import atlantis.placement.core.SupportRequest;

import java.util.Collections;
import java.util.List;

/**
 * Reserves a spot for a planned building (M3 seam). The real implementation
 * delegates to the existing position finder; tests install a fake.
 *
 * <p>
 * <b>Reservations are concrete for the duration of one planning pass.</b>
 * Without that, the same tile answers "free" to every building planned in the
 * same pass and two Pylons get ordered onto one spot - which is precisely the
 * class of duplicate the redesign exists to remove. The scheduler therefore
 * calls {@link #startPass()} before planning and {@link #endPass()} after, and
 * an implementation must treat tiles reserved during the pass as taken.
 * </p>
 */
public interface PlacementPlanner {

    /**
     * Tries to reserve a legal, buildable spot for the building under the
     * constraint. Returns a failed reservation when nothing fits - the
     * scheduler then skips the item this pass (it is recomputed next frame).
     */
    PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame);

    /**
     * Tries to reserve a legal, buildable spot for the building under the
     * given constraint, skipping the given already-refused tiles.
     *
     * <p>The default answers like {@link #reservePlacement} (no exclusions);
     * the catalogue planner honors the set. Tiles are named
     * {@code "tx:ty"}, like the per-pass reservation memory - a tile the
     * funnel's verdict just refused must not be re-served to the same
     * demand in the same call (measured live: the same cursed tile
     * re-served every frame until something else moved).
     */
    default PlacementReservation reservePlacementExcluding(Producible building, TargetPlacement constraint,
                                                           int targetFrame, java.util.Set<String> excludedTiles) {
        return reservePlacement(building, constraint, targetFrame);
    }

    /**
     * Called once before a schedule pass: spatial reservations from a previous
     * pass are discarded (the plan is stateless and recomputed every frame).
     */
    default void startPass() {
    }

    /**
     * Called once after a schedule pass. A live implementation may keep the
     * reservations around for the dispatcher to read; the next
     * {@link #startPass()} is what clears them.
     */
    default void endPass() {
    }

    /**
     * Race support declared during this pass (unpowered tiles answered
     * with a support request instead of a reservation). Drained - the call
     * clears what it returns, so each request is forwarded exactly once.
     * The default answers nothing; the composition root that ran the pass
     * forwards what it gets to production.
     */
    default List<SupportRequest> drainSupportRequests() {
        return Collections.emptyList();
    }
}
