package atlantis.placement.core;

/**
 * The placement preference order (`_AI/redesign/03_PLACEMENT.md` §2 Phase 6).
 *
 * <p>
 * Stardust's {@code BuildLocationCmp} is a strict lexicographic ordering with
 * eight levels, most of which are Protoss/block specifics (start-block pylon,
 * DT-detection pylon, refinery slot). The two that carry over to a general
 * planner, and that the legacy Atlantis finder had only as loose heuristics,
 * are:
 * </p>
 * <ol>
 * <li><b>available before unavailable</b>: a powered/ready tile beats one that
 * must wait for a Pylon;</li>
 * <li><b>distance, weighted by kind</b>: a normal building wants to be near the
 * exit (toward the map, so its units leave quickly), a tech building wants
 * to be <i>away</i> from it (Stardust's {@code buildAwayFromExit}), and
 * builder travel always counts against a tile.</li>
 * </ol>
 *
 * <p>
 * Pure: it compares values only, so it is unit-testable without a game.
 * </p>
 */
public final class BuildLocationRanker implements BuildLocationCatalogue.LocationScorer {

    private final NeighbourhoodRegistry neighbourhoods;
    private final boolean preferAwayFromExit;
    private final boolean forTechBuilding;

    public BuildLocationRanker(
            NeighbourhoodRegistry neighbourhoods, boolean preferAwayFromExit, boolean forTechBuilding) {
        this.neighbourhoods = neighbourhoods;
        this.preferAwayFromExit = preferAwayFromExit;
        this.forTechBuilding = forTechBuilding;
    }

    @Override
    public int compare(BuildLocation a, BuildLocation b) {
        // 1. Available now / sooner first. -1 (never) sorts last.
        int availability = Integer.compare(normalise(a.framesUntilAvailable()),
                normalise(b.framesUntilAvailable()));
        if (availability != 0)
            return availability;

        // 2. Distance score, lower is better.
        return Integer.compare(score(a), score(b));
    }

    /** {@code -1} means "never available" and must sort after every real frame. */
    private static int normalise(int framesUntilAvailable) {
        return framesUntilAvailable < 0 ? Integer.MAX_VALUE : framesUntilAvailable;
    }

    /**
     * {@code builderFrames * 2 +/- distanceToExit}. Tech buildings and rush mode
     * flip the exit term (Stardust rule 7): being far from the choke is a virtue
     * for a tech building and a cost for a Gateway.
     */
    private int score(BuildLocation location) {
        int travel = location.builderFrames() * 2;

        int distance = exitDistance(location);
        if (distance == Integer.MAX_VALUE) {
            // No known exit: fall back to travel only, so a base without a mapped
            // choke still gets usable placements instead of nothing.
            return travel;
        }

        boolean away = preferAwayFromExit || forTechBuilding;
        return away ? travel - distance : travel + distance;
    }

    private int exitDistance(BuildLocation location) {
        if (neighbourhoods == null)
            return Integer.MAX_VALUE;

        NeighbourhoodRegistry.Neighbourhood area = neighbourhoods.forTile(location.tileX(), location.tileY());
        return NeighbourhoodRegistry.distanceToExit(area, location.tileX(), location.tileY());
    }

    /** True for a building that should prefer being away from the choke. */
    public static boolean isTechBuilding(atlantis.units.AUnitType type) {
        return type != null && type.isProtossImportantTechBuilding();
    }
}
