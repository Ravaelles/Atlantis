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

    private SimConfig(Mode mode) {
        this.mode = mode;
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
