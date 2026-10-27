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
    /**
     * Frame the builder was actually committed on; -1 while still uncommitted.
     */
    private final int committedAtFrame;
    /**
     * Weapon coverage of the tile, set only by coverage-ranked answers; null when unknown.
     */
    private final Boolean coversChoke;
    private final Boolean coversAnchor;

    private PlacementReservation(boolean successful, int tileX, int tileY, int readyFrame, int committedAtFrame) {
        this(successful, tileX, tileY, readyFrame, committedAtFrame, null, null);
    }

    private PlacementReservation(boolean successful, int tileX, int tileY, int readyFrame, int committedAtFrame,
                                 Boolean coversChoke, Boolean coversAnchor) {
        this.successful = successful;
        this.tileX = tileX;
        this.tileY = tileY;
        this.readyFrame = readyFrame;
        this.committedAtFrame = committedAtFrame;
        this.coversChoke = coversChoke;
        this.coversAnchor = coversAnchor;
    }

    public static PlacementReservation success(int tileX, int tileY, int readyFrame) {
        return new PlacementReservation(true, tileX, tileY, readyFrame, -1);
    }

    public static PlacementReservation failure() {
        return new PlacementReservation(false, -1, -1, -1, -1);
    }

    /**
     * A reservation the dispatcher has acted on: the builder is walking or building.
     */
    public PlacementReservation committedAt(int frame) {
        return new PlacementReservation(successful, tileX, tileY, readyFrame, frame, coversChoke, coversAnchor);
    }

    /**
     * Coverage verdict from a coverage-ranked answer; unknown (null) otherwise.
     */
    public PlacementReservation withCoverage(boolean coversChoke, boolean coversAnchor) {
        return new PlacementReservation(successful, tileX, tileY, readyFrame, committedAtFrame,
            coversChoke, coversAnchor);
    }

    public Boolean coversChoke() {
        return coversChoke;
    }

    public Boolean coversAnchor() {
        return coversAnchor;
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

    /**
     * First frame the tile is actually usable (builder travel time included).
     */
    public int readyFrame() {
        return readyFrame;
    }

    /**
     * Frame the dispatcher committed a builder to this tile, or -1 when it has
     * not committed yet. The dispatcher uses it to keep re-committing a
     * pending construction (idempotently) instead of letting a builder that
     * died en route abandon the site.
     */
    public int committedAtFrame() {
        return committedAtFrame;
    }

    @Override
    public String toString() {
        if (!successful) return "no placement";
        return "tile(" + tileX + "," + tileY + ")@" + readyFrame
            + (committedAtFrame >= 0 ? " committed@" + committedAtFrame : "");
    }
}
