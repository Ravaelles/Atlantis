package tests.unit.sim;

/**
 * How the economy sim resolves the three model seams (movement, mining,
 * placement check). One codebase, two calibrations - never two engines.
 *
 * <p>FAST is the suite default: analytic travel, flat mining income,
 * full order validation except the tile-by-tile walk. FAITHFUL replays
 * the walk on the snapshot tiles for placement/travel disputes.
 */
public final class SimConfig {

    private final Mode mode;
    private final int stride;
    private final boolean visualize;

    private SimConfig(Mode mode) {
        this(mode, 1, false);
    }

    private SimConfig(Mode mode, int stride) {
        this(mode, Math.max(1, stride), false);
    }

    private SimConfig(Mode mode, int stride, boolean visualize) {
        this.mode = mode;
        this.stride = Math.max(1, stride);
        this.visualize = visualize;
    }

    /**
     * FAST-only knob: advance this many frames per engine update, scaling
     * income and build timers accordingly. The long-game sim (70 supply)
     * runs stride 30: ~570 engine updates instead of ~17000. Combat
     * scenarios keep 1 - striding skips the per-frame decisions they pin.
     */
    public SimConfig withStride(int stride) {
        return new SimConfig(mode, stride, visualize);
    }

    /**
     * Opt-in live view for exactly one test run: a Swing window shows the
     * snapshot map (bases, minerals, planned slots are drawn by the
     * painter, not here) plus every sim unit, refreshed per step and
     * closed automatically a few seconds after the test ends. Headless
     * environments (CI, no X display) silently skip it, so the flag is
     * safe to leave in a test - but leave it OFF unless you are
     * watching this run, as an open window holds the JVM open.
     */
    public SimConfig withVisualization() {
        return new SimConfig(mode, stride, true);
    }

    public boolean visualize() {
        return visualize || Boolean.getBoolean("atlantis.visualize");
    }

    public int stride() {
        return mode == Mode.FAST ? stride : 1;
    }

    public static SimConfig fast() {
        return new SimConfig(Mode.FAST);
    }

    public static SimConfig faithful() {
        return new SimConfig(Mode.FAITHFUL);
    }

    public boolean isFast() {
        return mode == Mode.FAST;
    }

    public boolean isFaithful() {
        return mode == Mode.FAITHFUL;
    }

    @Override
    public String toString() {
        return "SimConfig{" + mode + '}';
    }

    public enum Mode {
        /**
         * Analytic travel, flat income, full order validation.
         */
        FAST,
        /**
         * Tile-walked travel, trip mining, full order validation.
         */
        FAITHFUL
    }
}
