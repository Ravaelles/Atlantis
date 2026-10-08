package atlantis.placement.core;

/**
 * One candidate spot in the catalogue: a tile a building of a given footprint
 * can
 * stand on, with the facts the ranker needs (`_AI/redesign/03_PLACEMENT.md` §2
 * Step 5.1).
 *
 * <p>
 * Immutable value. The ranker orders these; the planner takes the best one. The
 * legacy finder returned a single tile and nothing to choose between, which is
 * why ranking had nowhere to happen before.
 * </p>
 */
public final class BuildLocation {

    private final int tileX;
    private final int tileY;
    private final int tileWidth;
    private final int tileHeight;

    /**
     * Approximate worker travel time in frames from the nearest worker to this
     * tile. An estimate (Stardust has the same TODO) - it decides ordering, never
     * correctness.
     */
    private final int builderFrames;

    /**
     * Frames until the tile can actually host the building: {@code 0} when it is
     * usable now, a positive frame count when something must complete first
     * (Protoss: a Pylon finishing), {@code -1} when nothing can ever make it
     * usable.
     */
    private final int framesUntilAvailable;

    /** Ground distance to the neighbourhood's exit (main choke), in build tiles. */
    private final int distanceToExit;

    /** True for a location a tech building should prefer (see the ranker). */
    private final boolean techLocation;

    /**
     * Medium slots only: whether a building here can send units toward the map
     * (Stardust's {@code hasExit}). Medium slots without an exit are preferred for
     * buildings that do not need one, leaving exit-capable tiles free.
     */
    private final boolean hasExit;

    public BuildLocation(
            int tileX, int tileY, int tileWidth, int tileHeight,
            int builderFrames, int framesUntilAvailable, int distanceToExit, boolean techLocation) {
        this(tileX, tileY, tileWidth, tileHeight, builderFrames, framesUntilAvailable,
            distanceToExit, techLocation, true);
    }

    public BuildLocation(
            int tileX, int tileY, int tileWidth, int tileHeight,
            int builderFrames, int framesUntilAvailable, int distanceToExit,
            boolean techLocation, boolean hasExit) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.builderFrames = builderFrames;
        this.framesUntilAvailable = framesUntilAvailable;
        this.distanceToExit = distanceToExit;
        this.techLocation = techLocation;
        this.hasExit = hasExit;
    }

    /**
     * The same tile with the two computed facts filled in. The catalogue emits
     * geometry it can know without workers; the planner refines each candidate
     * with a real travel estimate and the neighbourhood's exit distance.
     */
    public BuildLocation refined(int builderFrames, int distanceToExit) {
        return new BuildLocation(
            tileX, tileY, tileWidth, tileHeight,
            builderFrames, framesUntilAvailable, distanceToExit, techLocation, hasExit
        );
    }

    /** The same tile with its availability frame resolved. */
    public BuildLocation withFramesUntilAvailable(int framesUntilAvailable) {
        return new BuildLocation(
            tileX, tileY, tileWidth, tileHeight, builderFrames, framesUntilAvailable,
            distanceToExit, techLocation, hasExit
        );
    }

    public boolean hasExit() {
        return hasExit;
    }

    public int tileX() {
        return tileX;
    }

    public int tileY() {
        return tileY;
    }

    public int tileWidth() {
        return tileWidth;
    }

    public int tileHeight() {
        return tileHeight;
    }

    public int builderFrames() {
        return builderFrames;
    }

    public int framesUntilAvailable() {
        return framesUntilAvailable;
    }

    /** False when nothing can ever make this location usable. */
    public boolean everAvailable() {
        return framesUntilAvailable >= 0;
    }

    public boolean availableNow() {
        return framesUntilAvailable == 0;
    }

    public int distanceToExit() {
        return distanceToExit;
    }

    public boolean isTechLocation() {
        return techLocation;
    }

    @Override
    public String toString() {
        return "loc(" + tileX + "," + tileY + " " + tileWidth + "x" + tileHeight
                + " builder=" + builderFrames
                + " avail=" + framesUntilAvailable
                + " exit=" + distanceToExit
                + (techLocation ? " tech" : "") + ")";
    }
}
