package atlantis.placement.core;

/**
 * Which tiles a ground unit may stand on. The input to every reachability
 * question.
 *
 * <p>
 * This exists because {@code Game.hasPath} - the engine's own answer to "can
 * something
 * get from A to B" - is implemented over BWAPI <b>regions</b>, and OpenBW
 * publishes
 * {@code regionCount = 0}, so it answers {@code false} for every pair of tiles
 * on the
 * map (measured 2026-10-10, {@code _AI/CHALLENGES/OpenBW.md}). That answer
 * reaches us
 * through {@code Game.canBuildHere}'s builder-reachability check, which is why
 * {@code Unit.build(...)} refuses a tile the builder is standing on.
 * </p>
 *
 * <p>
 * A walkability bitmap is the thing the engine <i>does</i> publish, and it is
 * what both
 * reference projects rely on: Stardust runs its own A* over its own navigation
 * grids,
 * PurpleWave asks {@code game.isWalkable} at the corners of a footprint.
 * Neither ever
 * asks the engine about a path. This is the same idea, as a seam: pure, no
 * BWAPI, so
 * connectivity can be tested on a synthetic map instead of in a game.
 * </p>
 */
public interface WalkabilityGrid {

    int mapWidth();

    int mapHeight();

    /**
     * True when a ground unit may stand on this tile. Off-map answers
     * {@code false}.
     */
    boolean isWalkable(int tx, int ty);
}
