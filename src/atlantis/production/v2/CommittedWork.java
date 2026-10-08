package atlantis.production.v2;

/**
 * Work the game has already committed to, applied to a fresh timeline before
 * the scheduler runs ({@code initializeResources} in 01_PRODUCTION.md).
 *
 * <p>
 * BWAPI accounting: a building's cost leaves the bank when it is
 * <em>placed</em>
 * and a unit's when it is <em>queued</em>. So a requested construction whose
 * builder is still walking is not paid yet and must be reserved here; a
 * started one is already out of the stocks and must not be charged twice.
 * Supply of a queued unit is already in {@code supplyUsed}; supply a provider
 * under construction will add arrives at its completion.
 * </p>
 */
public final class CommittedWork {

    private CommittedWork() {
    }

    /**
     * An unpaid construction: reserves its cost at the earliest frame we can
     * pay it (never on credit). Returns that frame, or -1 when the horizon
     * cannot pay it - the stocks are then left untouched.
     */
    public static int reserveUnpaid(ResourceTimeline timeline, ResourceCost cost) {
        ResourceCost noSupply = ResourceCost.of(cost.minerals(), cost.gas(), 0);
        int frame = timeline.findEarliestAffordableFrame(noSupply, timeline.originFrame());
        if (frame >= 0)
            timeline.allocate(noSupply, frame);
        return frame;
    }

    /** A supply provider under construction: its supply arrives at completion. */
    public static void providerCompletesAt(ResourceTimeline timeline, int completionFrame, int supplyProvided) {
        if (supplyProvided > 0)
            timeline.addSupplyFrom(completionFrame, supplyProvided);
    }

    /**
     * A worker in training: starts paying back after completion plus its first
     * trip.
     */
    public static void workerCompletesAt(ResourceTimeline timeline, int completionFrame) {
        timeline.addMiningIncome(completionFrame + EconomyModel.NEW_WORKER_FIRST_TRIP_FRAMES,
                EconomyModel.MINERALS_PER_WORKER_FRAME, 0);
    }
}
