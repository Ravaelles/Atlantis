package atlantis.production.v2;

/**
 * Frame-indexed projection of minerals, gas and supply over a planning horizon.
 *
 * <p>
 * The heart of the stateless production model (01_PRODUCTION.md): resources
 * are represented as a forward timeline starting from current BWAPI stocks and
 * the estimated mining rate. Scheduling an item at frame F deducts its cost
 * from every frame >= F, so a later item cannot steal funds an earlier one
 * already reserved - and an unaffordable item is <b>shifted forward</b>, never
 * dropped.
 * </p>
 *
 * <p>
 * Pure domain (DIP): no BWAPI anywhere. The engine seeds it from the game;
 * tests seed it directly, so the whole solver is deterministic in JUnit.
 * </p>
 *
 * <p>
 * Solvency invariant (enforced by tests): after any sequence of allocations
 * the balance never goes below zero at any frame - a negative balance would
 * mean the plan promises something the economy cannot pay for.
 * </p>
 */
public final class ResourceTimeline {

    private final int horizon;
    private final int[] minerals;
    private final int[] gas;
    private final int[] supplyAvailable;

    public ResourceTimeline(int horizon, int startMinerals, int startGas, int startSupplyAvailable) {
        if (horizon <= 0)
            throw new IllegalArgumentException("Horizon must be positive: " + horizon);
        if (startMinerals < 0 || startGas < 0 || startSupplyAvailable < 0) {
            throw new IllegalArgumentException("Negative start stocks: "
                    + startMinerals + "/" + startGas + "/" + startSupplyAvailable);
        }

        this.horizon = horizon;
        this.minerals = new int[horizon];
        this.gas = new int[horizon];
        this.supplyAvailable = new int[horizon];
        java.util.Arrays.fill(this.minerals, startMinerals);
        java.util.Arrays.fill(this.gas, startGas);
        java.util.Arrays.fill(this.supplyAvailable, startSupplyAvailable);
    }

    /**
     * Adds linear mining income from {@code fromFrame} to the end of the
     * horizon. Rates are doubles per frame (e.g. ~0.045 minerals/frame/worker
     * on fully saturated patches); accumulated as fractional and floored per
     * frame so the last frames do not lose a large remainder.
     */
    public void addMiningIncome(int fromFrame, double mineralsPerFrame, double gasPerFrame) {
        if (fromFrame < 0) fromFrame = 0;

        // Cumulative income floors: frame F holds floor(total income up to F).
        // Computing the cumulative total first (rather than per-frame deltas)
        // keeps whole-rate income strictly linear - the per-frame approach lost
        // the accumulation when a frame's fractional part was zero (measured:
        // rate 1.0 produced 1 mineral at every frame instead of F+1).
        double mineralsTotal = 0;
        double gasTotal = 0;
        for (int frame = 0; frame < horizon; frame++) {
            if (frame >= fromFrame) {
                mineralsTotal += mineralsPerFrame;
                gasTotal += gasPerFrame;
            }
            minerals[frame] += (int) mineralsTotal;
            gas[frame] += (int) gasTotal;
        }
    }

    /** Supply freed (or removed) from a given frame on, e.g. a Pylon finishing. */
    public void addSupplyFrom(int fromFrame, int supplyDelta) {
        if (fromFrame < 0)
            fromFrame = 0;
        for (int frame = fromFrame; frame < horizon; frame++) {
            supplyAvailable[frame] += supplyDelta;
        }
    }

    /**
     * First frame at which all three stocks cover the cost - scanning forward
     * from {@code afterFrame}. Returns -1 when the horizon cannot afford it
     * (the caller shifts the goal forward into the next planning pass rather
     * than dropping it).
     */
    public int findEarliestAffordableFrame(ResourceCost cost, int afterFrame) {
        for (int frame = Math.max(0, afterFrame); frame < horizon; frame++) {
            if (minerals[frame] >= cost.minerals()
                    && gas[frame] >= cost.gas()
                    && supplyAvailable[frame] >= cost.supply()) {
                return frame;
            }
        }
        return -1;
    }

    /** True when the cost is covered at exactly this frame. */
    public boolean canAffordAt(ResourceCost cost, int frame) {
        if (frame < 0 || frame >= horizon)
            return false;
        return minerals[frame] >= cost.minerals()
                && gas[frame] >= cost.gas()
                && supplyAvailable[frame] >= cost.supply();
    }

    /**
     * Deducts the cost from every frame >= atFrame (the reserved money is gone
     * for all later plans too). Call only after {@link #canAffordAt} - the
     * solvency assertion below is the guard.
     */
    public void allocate(ResourceCost cost, int atFrame) {
        if (atFrame < 0)
            atFrame = 0;
        for (int frame = atFrame; frame < horizon; frame++) {
            minerals[frame] -= cost.minerals();
            gas[frame] -= cost.gas();
            supplyAvailable[frame] -= cost.supply();
        }

        assertSolvency();
    }

    /**
     * The solvency invariant as a hard check (assertions are off in production
     * JVMs, so this is an explicit check in debug builds and in tests - the
     * invariant is cheap to verify once per allocation, not per frame).
     */
    private void assertSolvency() {
        for (int frame = 0; frame < horizon; frame++) {
            if (minerals[frame] < 0 || gas[frame] < 0 || supplyAvailable[frame] < 0) {
                throw new IllegalStateException("ResourceTimeline solvency violated at frame " + frame
                        + ": " + minerals[frame] + "m/" + gas[frame] + "g/" + supplyAvailable[frame] + "s");
            }
        }
    }

    public int mineralsAt(int frame) {
        return minerals[clamp(frame)];
    }

    public int gasAt(int frame) {
        return gas[clamp(frame)];
    }

    public int supplyAvailableAt(int frame) {
        return supplyAvailable[clamp(frame)];
    }

    public int horizon() {
        return horizon;
    }

    private int clamp(int frame) {
        if (frame < 0)
            return 0;
        if (frame >= horizon)
            return horizon - 1;
        return frame;
    }
}
