package tests.unit.sim;

/**
 * A sim that stops progressing is a failed test, never a slow one.
 *
 * <p>Every {@link SimDriver} run is covered by two global guards, checked
 * every step: a 20 game-minute cap (36000 frames - a sim that long without
 * hitting its own cap is hung) and a hoard watch (800+ banked minerals
 * with no warp for 1500 frames - saving ends in a warp, a stall does
 * not). Both throw this, an {@link AssertionError}, so the run fails
 * loudly instead of timing out silently after dozens of wasted runs.</p>
 */
public final class SimStallException extends AssertionError {

    /** 20 game-minutes: a sim that long without hitting its own cap is hung. */
    public static final int TIME_CAP_FRAMES = 30 * 60 * 20;
    /** Saving ends in a warp; this much bank with no warp is a stall. */
    public static final int HOARD_BANK = 800;
    /** Silence window before a hoard becomes a verdict. */
    public static final int HOARD_SILENT_FRAMES = 1500;

    private SimStallException(String message) {
        super(message);
    }

    public static SimStallException timeLimit(int frame, int capFrames) {
        return new SimStallException(
            "sim hit the " + capFrames + "-frame cap at f" + frame
                + " - the game never ended where it should have. "
                + "A sim that needs more frames is a frozen plan, not a long game.");
    }

    /**
     * The global SimDriver watchdog calls this every step (cheap: two int
     * compares). Pure - unit-testable without running a frame.
     */
    public static void check(int frame, int bankMinerals, int lastWarpFrame) {
        if (frame >= TIME_CAP_FRAMES) throw timeLimit(frame, TIME_CAP_FRAMES);
        if (bankMinerals >= HOARD_BANK && frame - lastWarpFrame >= HOARD_SILENT_FRAMES) {
            throw bankHoard(frame, bankMinerals, lastWarpFrame);
        }
    }

    public static SimStallException bankHoard(int frame, int bank, int sinceFrame) {
        return new SimStallException(
            "bank-stuck at f" + frame + ": " + bank
                + " minerals banked with no warp since f" + sinceFrame
                + " - saving ends in a warp, a stall does not. "
                + "Fix the producer, not the bank.");
    }
}
