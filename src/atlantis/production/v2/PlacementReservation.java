package atlantis.production.v2;

/**
 * Where a planned building will stand. Pure value - the planner port resolves
 * it; M3 introduces the real planner, tests use a fake that always answers
 * "this exact tile works".
 */
public final class PlacementReservation {

    private final boolean successful;
    private final int tileX;
    private final int tileY;
    private final int readyFrame;

    private PlacementReservation(boolean successful, int tileX, int tileY, int readyFrame) {
        this.successful = successful;
        this.tileX = tileX;
        this.tileY = tileY;
        this.readyFrame = readyFrame;
    }

    public static PlacementReservation success(int tileX, int tileY, int readyFrame) {
        return new PlacementReservation(true, tileX, tileY, readyFrame);
    }

    public static PlacementReservation failure() {
        return new PlacementReservation(false, -1, -1, -1);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public int tileX() {
        return tileX;
    }

    public int tileY() {
        return tileY;
    }

    /** First frame the tile is actually usable (builder travel time included). */
    public int readyFrame() {
        return readyFrame;
    }
}
