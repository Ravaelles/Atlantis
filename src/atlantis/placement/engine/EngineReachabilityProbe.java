package atlantis.placement.engine;

import atlantis.placement.core.Reachability;

/**
 * Decides which {@link Reachability} is trustworthy in the current game, by
 * asking the
 * engine a question it cannot get right without working regions.
 *
 * <p>
 * <b>The detection, not a guess.</b> The engine's {@code hasPath} is
 * region-based. When
 * regions exist, a tile trivially has a path to <b>itself</b>. When they do not
 * - OpenBW
 * publishes {@code regionCount = 0} - the query finds no region for either
 * endpoint and
 * answers {@code false} for every pair, including that one (measured
 * 2026-10-10,
 * {@code _AI/CHALLENGES/OpenBW.md}). So one query, on one tile, paired with
 * itself,
 * separates a healthy engine from a region-less one.
 * </p>
 *
 * <p>
 * That is deliberately a <b>capability probe rather than a version check</b>:
 * it does not
 * care which engine, which jar or which build is running, only whether the
 * question it is
 * about to rely on gets a sane answer. A future engine that publishes regions
 * correctly
 * passes automatically, and one that breaks in a new way fails, without this
 * class
 * needing to know about either.
 * </p>
 *
 * <p>
 * The probe result is cached for the game: regions are published once per game
 * in the
 * server's map data, so re-asking every frame would cost a query for an answer
 * that
 * cannot change mid-game - and, more importantly, a single measured answer is
 * easier to
 * reason about than one that might differ between calls.
 * </p>
 */
public final class EngineReachabilityProbe {

    private EngineReachabilityProbe() {
    }

    /**
     * Asks the engine whether a tile can reach itself. A sane answer means the
     * engine's
     * reachability is usable; {@code false} means its regions are missing and every
     * answer it gives about reachability, including the builder-reachability check
     * inside
     * {@code canBuild}, is worthless here.
     *
     * @param walkableTile a tile known to be walkable in the current game; the
     *                     probe is
     *                     meaningless on an unwalkable tile, where {@code false} is
     *                     correct and says nothing about regions
     */
    public static Verdict verdictFor(SelfReachabilityQuery query, int walkableTileX, int walkableTileY) {
        if (query == null) {
            return Verdict.ENGINE_REGIONS_MISSING;
        }

        boolean selfReachable = query.tileReachesItself(walkableTileX, walkableTileY);

        return selfReachable ? Verdict.ENGINE_USABLE : Verdict.ENGINE_REGIONS_MISSING;
    }

    /**
     * Who can answer the reachability question, in the order worth trying.
     */
    public enum Verdict {
        /**
         * The engine answered sanely: its own {@code hasPath} can be trusted.
         */
        ENGINE_USABLE,

        /**
         * The engine cannot reach a tile from itself: its regions are missing.
         */
        ENGINE_REGIONS_MISSING
    }

    /**
     * The question asked, as a function: does this tile reach itself?
     */
    public interface SelfReachabilityQuery {
        boolean tileReachesItself(int tx, int ty);
    }
}
