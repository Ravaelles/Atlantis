package atlantis.map;

import atlantis.config.env.Env;
import bwapi.Game;
import bwapi.Position;
import bwapi.Region;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * The single door to the engine's <b>terrain, path and region</b> queries - and
 * the guard that makes a broken one impossible to use by accident.
 *
 * <p>
 * OpenBW answers several of these incorrectly, deterministically, for a whole
 * run.
 * Measured 2026-10-10 ({@code _AI/CHALLENGES/OpenBW-API.md}):
 * </p>
 *
 * <ul>
 * <li>{@code hasPath} returns {@code false} for every pair - even a point to
 * itself. It is a region-group comparison, and the engine's region table is
 * empty
 * in this harness.</li>
 * <li>{@code getRegionAt} therefore returns {@code null} for every
 * position.</li>
 * <li>{@code canBuildHere} returns {@code false} for every tile, because its
 * last
 * check is {@code hasPath}.</li>
 * </ul>
 *
 * <p>
 * The damage these did was not that they were wrong - it was that they were
 * wrong
 * <b>quietly</b>: twelve production call sites read a {@code false} from
 * {@code hasPath} and simply took the "no path" branch, so expansion, attack
 * targeting and worker retreat were silently disabled on OpenBW with nothing in
 * any
 * log. A wrong answer that is loud is a bug report; a wrong answer that is
 * silent is
 * a week of investigation.
 * </p>
 *
 * <p>
 * So the methods here do two things: they are the only sanctioned way to ask
 * the
 * engine these questions, and on OpenBW they <b>throw</b> instead of answering.
 * A caller that reaches one on OpenBW has made a mistake - it should be using
 * our
 * own model ({@code MapTiles}, {@code AMap.getMap()}, {@code Pathfinding}) -
 * and it
 * must find that out at that line, not from a game that mysteriously never
 * expands.
 * </p>
 *
 * <p>
 * What is <b>not</b> guarded, because the survey measured it correct on OpenBW:
 * {@code isWalkable}, {@code isBuildable}, {@code isExplored},
 * {@code isVisible}.
 * Those are the queries our terrain model is built from, so guarding them would
 * break the very analysis that works.
 * </p>
 */
public final class EngineQueries {

    /** Thrown when an OpenBW run asks a question that engine cannot answer. */
    public static class UnreliableOnOpenBw extends RuntimeException {
        public UnreliableOnOpenBw(String query, String useInstead) {
            super("OpenBW cannot answer " + query + ": it is a region-group lookup and"
                    + " the engine's region table is empty in this harness, so it returns a"
                    + " constant (false/null) for every input. Using it silences whatever it"
                    + " gates. Use " + useInstead + " instead"
                    + " (_AI/CHALLENGES/OpenBW-API.md, _AI/CHALLENGES/TerrainAnalysis.md).");
        }
    }

    private static final String HAS_PATH_ALTERNATIVE = "MapTiles.hasPathBetween / jbweb.Pathfinding.reachable (a flood fill over our "
            + "own walkGrid), or AMap.getMap().getArea(...).isAccessibleFrom(...) (our "
            + "BWEM area graph)";

    private static final String REGION_ALTERNATIVE = "our BWEM model: AMap.getMap().getArea(walkPosition), AMap.getMap().getAreas(), "
            + "atlantis.map.region.Regions";

    private static final String CAN_BUILD_ALTERNATIVE = "MapTiles.canBuildHere (our terrain + exploration + occupancy answer)";

    private EngineQueries() {
    }

    // ===========================================================================
    // Guarded: broken on OpenBW
    // ===========================================================================

    /**
     * Can something walk from one point to another?
     *
     * @deprecated on OpenBW this answers a constant {@code false}. Use
     *             {@link MapTiles#hasPathBetween} or
     *             {@code jbweb.Pathfinding.reachable}. Kept for the non-OpenBW
     *             engines,
     *             where it is correct and is the cheapest available answer.
     */
    @Deprecated
    public static boolean hasPath(Game game, Position from, Position to) {
        if (Env.isOpenBW())
            throw new UnreliableOnOpenBw("Game.hasPath", HAS_PATH_ALTERNATIVE);
        return game.hasPath(from, to);
    }

    /**
     * As {@link #hasPath}, from a unit.
     *
     * @deprecated on OpenBW this answers a constant {@code false}. Use
     *             {@link MapTiles#hasPathBetween}.
     */
    @Deprecated
    public static boolean hasPath(Unit unit, Position to) {
        if (Env.isOpenBW())
            throw new UnreliableOnOpenBw("Unit.hasPath", HAS_PATH_ALTERNATIVE);
        return unit.hasPath(to);
    }

    /**
     * The engine's region at a position.
     *
     * @deprecated on OpenBW the region table is empty, so this returns {@code null}
     *             for every position. Use our BWEM model
     *             ({@code AMap.getMap().getArea}).
     */
    @Deprecated
    public static Region getRegionAt(Game game, Position at) {
        if (Env.isOpenBW())
            throw new UnreliableOnOpenBw("Game.getRegionAt", REGION_ALTERNATIVE);
        return game.getRegionAt(at);
    }

    /**
     * Can {@code builder} put {@code building} on this tile, with the engine's own
     * path check included?
     *
     * @deprecated on OpenBW this answers a constant {@code false}, because its last
     *             precondition is the broken {@code hasPath}. Use
     *             {@link MapTiles#canBuildHere}.
     */
    @Deprecated
    public static boolean canBuildHere(Game game, TilePosition at, UnitType building, Unit builder) {
        if (Env.isOpenBW())
            throw new UnreliableOnOpenBw("Game.canBuildHere", CAN_BUILD_ALTERNATIVE);
        return game.canBuildHere(at, building, builder, true);
    }

    // ===========================================================================
    // Safe everywhere (measured correct on OpenBW) - kept here so the split is
    // visible in one file rather than inferred from a survey report.
    // ===========================================================================

    /** Safe on OpenBW (measured). Walkability, one 8x8 walk cell. */
    public static boolean isWalkable(Game game, TilePosition tile) {
        return game.isWalkable(tile.toWalkPosition());
    }

    /** Safe on OpenBW (measured). Terrain, ignoring units and buildings. */
    public static boolean isBuildable(Game game, int tx, int ty) {
        return game.isBuildable(tx, ty);
    }

    /** Safe on OpenBW (measured). Terrain plus what already stands on it. */
    public static boolean isBuildableIncludeBuildings(Game game, int tx, int ty) {
        return game.isBuildable(tx, ty, true);
    }

    /** Safe on OpenBW (measured). */
    public static boolean isExplored(Game game, TilePosition tile) {
        return game.isExplored(tile.getX(), tile.getY());
    }

    /** Safe on OpenBW (measured). */
    public static boolean isVisible(Game game, TilePosition tile) {
        return game.isVisible(tile.getX(), tile.getY());
    }
}
