package atlantis.production.v2;

/**
 * Reserves a spot for a planned building (M3 seam). The real implementation
 * delegates to the existing position finder; tests install a fake.
 */
public interface PlacementPlanner {

    /**
     * Tries to reserve a legal, buildable spot for the building under the
     * constraint. Returns a failed reservation when nothing fits - the
     * scheduler then skips the item this pass (it is recomputed next frame).
     */
    PlacementReservation reservePlacement(Producible building, TargetPlacement constraint, int targetFrame);
}
