package atlantis.production.v2;

/**
 * Spatial constraint for a building, as a value with three modes.
 *
 * <p>
 * Pure domain: carries no engine objects. The placement planner resolves the
 * constraint against the map through its own port, so a test can install a
 * fake and the scheduler stays deterministic.
 * </p>
 */
public final class TargetPlacement {

    public enum Mode {
        ANYWHERE, NEIGHBOURHOOD, EXACT_TILE
    }

    private final Mode mode;
    /** Neighbourhood centre in build tiles; meaningful for NEIGHBOURHOOD. */
    private final int tileX;
    private final int tileY;
    /**
     * Exact tile; meaningful for EXACT_TILE (ramp pylons, cannon creeps, geysers).
     */
    private final int exactTileX;
    private final int exactTileY;

    private static final TargetPlacement ANYWHERE = new TargetPlacement(Mode.ANYWHERE, 0, 0, 0, 0);

    private TargetPlacement(Mode mode, int tileX, int tileY, int exactTileX, int exactTileY) {
        this.mode = mode;
        this.tileX = tileX;
        this.tileY = tileY;
        this.exactTileX = exactTileX;
        this.exactTileY = exactTileY;
    }

    public static TargetPlacement anywhere() {
        return ANYWHERE;
    }

    public static TargetPlacement inNeighbourhood(int tileX, int tileY) {
        return new TargetPlacement(Mode.NEIGHBOURHOOD, tileX, tileY, 0, 0);
    }

    public static TargetPlacement exactTile(int tileX, int tileY) {
        return new TargetPlacement(Mode.EXACT_TILE, 0, 0, tileX, tileY);
    }

    public Mode mode() {
        return mode;
    }

    public int tileX() {
        return tileX;
    }

    public int tileY() {
        return tileY;
    }

    public int exactTileX() {
        return exactTileX;
    }

    public int exactTileY() {
        return exactTileY;
    }

    @Override
    public String toString() {
        switch (mode) {
            case ANYWHERE:
                return "anywhere";
            case NEIGHBOURHOOD:
                return "near(" + tileX + "," + tileY + ")";
            case EXACT_TILE:
                return "exact(" + exactTileX + "," + exactTileY + ")";
        }
        return mode.name();
    }
}
