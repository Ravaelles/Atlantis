package atlantis.map;

import atlantis.Atlantis;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.BuildingTilesAreOccupied;
import jbweb.JBWEB;

/**
 * What the map knows about a single tile: is it walkable, has it been explored,
 * is it visible now, can a building stand there - and, for two tiles, can
 * something walk from one to the other.
 *
 * <p>In a game these four answers come from the engine
 * ({@code bwapi.Game.isWalkable}, {@code isExplored}, {@code isVisible},
 * {@code isBuildable}). The stub world has no map behind it, so the harness
 * answers instead - and until now it did so from inside production code: every
 * caller wrote its own {@code if (Env.isTesting()) return ...} branch in front of
 * the engine call, five times over in {@code HasPosition} alone. A port says which
 * of the two is in charge, so the branches live in one place and the fake world
 * gets to change its mind per question.</p>
 *
 * <p>The answers are byte-for-byte the ones the branches gave: everything is
 * walkable and visible, buildable means "no unit within 1.98 tiles", and explored
 * is a flag the harness flips for the tests that need a position nobody
 * has seen.</p>
 */
public class MapTiles {
    public interface Source {
        boolean isWalkable(HasPosition at);

        boolean isExplored(HasPosition at);

        boolean isVisible(HasPosition at);

        /**
         * @param alsoCheckBuildings whether buildings on the tile count as blocking,
         *                           like {@code Game.isBuildable(int, int, boolean)}.
         */
        boolean isBuildable(HasPosition at, boolean alsoCheckBuildings);

        /**
         * "Can a building of this type stand on this tile?" The engine answers in
         * a game; the harness answers from its own rules. A <b>refusal that is
         * neither an engine answer nor a harness rule</b> is what the default
         * below is for - see the note in {@code canBuildHere}.
         */
        default boolean spawnsFromMapData() {
            return true;
        }

        /**
         * Is there a walkable path from one tile to the other? The stub world has
         * no pathing graph, so it answers yes - which is what lets a test place two
         * bases 40 tiles apart without building a map for them.
         */
        boolean hasPathBetween(HasPosition from, HasPosition to);

        /**
         * Can {@code builder} put {@code building} down on this tile? A game asks
         * the engine, which knows about power, terrain and the buildings already
         * standing there. The stub world has no engine answer, so it approximates
         * one from the units it does have ({@code tests.fakes.FakeMapTiles}).
         *
         * @param builder may be a double, which has no engine object; the harness's
         *                 answer does not need one.
         */
        boolean canBuildHere(AUnit builder, AUnitType building, APosition at);
    }

    private static Source source = null;

    public static void useSource(Source newSource) {
        source = newSource;
    }

    public static void useEngine() {
        source = null;
    }

    private static Source source() {
        return source != null ? source : EngineSource;
    }

    private static final Source EngineSource = new Source() {
        @Override
        public boolean isWalkable(HasPosition at) {
            return Atlantis.game().isWalkable(at.position().p().toWalkPosition());
        }

        @Override
        public boolean isExplored(HasPosition at) {
            return Atlantis.game().isExplored(at.position().p().toTilePosition());
        }

        @Override
        public boolean isVisible(HasPosition at) {
            return Atlantis.game().isVisible(at.position().p().toTilePosition());
        }

        @Override
        public boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
            return alsoCheckBuildings
                ? Atlantis.game().isBuildable(at.tx(), at.ty(), true)
                : Atlantis.game().isBuildable(at.position().p().toTilePosition());
        }

        @Override
        public boolean hasPathBetween(HasPosition from, HasPosition to) {
            // This is the ONE place allowed to swallow the guard's exception: it is
            // the seam whose contract is "ask the engine, else I have nothing", and
            // the caller above turns that into our own answer. Everywhere else the
            // exception must reach the caller.
            try {
                return EngineQueries.hasPath(Atlantis.game(), from.position().p(), to.position().p());
            } catch (EngineQueries.UnreliableOnOpenBw e) {
                announceFallbackOnce("Game.hasPath", "our own walkGrid flood fill");
                return false;
            }
        }

        @Override
        public boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
            try {
                return EngineQueries.canBuildHere(Atlantis.game(), at.toTilePosition(), building.ut(), builder.u());
            } catch (EngineQueries.UnreliableOnOpenBw e) {
                announceFallbackOnce("Game.canBuildHere", "our terrain + exploration + occupancy answer");
                return false;
            }
        }
    };

    /**
     * Says once, loudly, that the engine could not answer and we are using our own
     * model. Once per game, not per call: this fires on a hot path, but silence is
     * what made the original bug invisible, so it must not be silent either.
     */
    private static final java.util.Set<String> announcedFallbacks = new java.util.HashSet<>();

    private static void announceFallbackOnce(String query, String usingInstead) {
        if (!announcedFallbacks.add(query)) return;

        System.out.println("MAPTILES: the engine cannot answer " + query
            + " (OpenBW returns a constant - _AI/CHALLENGES/OpenBW-API.md);"
            + " using " + usingInstead + " instead. This is expected on OpenBW.");
    }

    /** Test hook: the fallback notice is once per game, so tests must reset it. */
    public static void resetFallbackNotices() {
        announcedFallbacks.clear();
    }

    // =========================================================

    public static boolean isWalkable(HasPosition at) {
        return source().isWalkable(at);
    }

    public static boolean isExplored(HasPosition at) {
        return source().isExplored(at);
    }

    public static boolean isVisible(HasPosition at) {
        return source().isVisible(at);
    }

    public static boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
        return source().isBuildable(at, alsoCheckBuildings);
    }

    public static boolean hasPathBetween(HasPosition from, HasPosition to) {
        if (source().hasPathBetween(from, to)) return true;

        // OpenBW's pathing query answers "no path" for every pair on
        // (3)TauCross1.1, which left the natural base undetermined and stopped the
        // bot from expanding. The reason is now known and is not a bad map: the
        // engine's hasPath is a region-group comparison and its region table is
        // empty in this harness, so it cannot reach even a tile to itself
        // (_AI/CHALLENGES/OpenBW-API.md).
        //
        // So the fallback asks OUR OWN model instead - a flood fill over our walkGrid
        // (jbweb.Pathfinding.reachable, built from Game.isWalkable, which the engine
        // answers correctly). This used to be `JBWEB.isWalkable(to)`: a walkability
        // check on the DESTINATION TILE ONLY, which says "yes" for a tile across an
        // unpassable wall and so was never a reachability answer at all.
        //
        // The map-derived answer applies only to the engine source, never to the
        // harness: the harness has its own rules and no map to consult.
        if (!source().spawnsFromMapData()) return false;
        if (from == null || to == null) return false;

        // Our BWEM already answers this for whole areas, and answers it the same way
        // the engine was supposed to: an area graph built from tiles, with
        // isAccessibleFrom for connectivity and chokepoint paths for the route
        // (bwem.Area, used by ARegion and PathToEnemyBase). It is cheap - a graph
        // walk, no per-tile fill - so it is asked first, and the tile flood fill
        // below is the fallback for the cases the area graph cannot speak about:
        // a point outside any area, or two points inside the SAME area, where
        // "accessible" says nothing about a local obstacle (a wall, a building).
        Boolean byArea = accessibleThroughAreas(from, to);
        if (byArea != null) return byArea;

        return jbweb.Pathfinding.reachable(
            asTile(from), asTile(to)
        );
    }

    /**
     * The area-graph answer, or {@code null} when the areas cannot decide.
     *
     * <p>
     * Returns {@code true} only for two points in <b>different</b> areas that BWEM
     * says are mutually accessible - a whole-map question the area graph is exactly
     * right for. Same-area and out-of-area pairs return {@code null} so the caller
     * falls through to the tile flood fill: two points in one area can still be
     * separated by a wall or a building, and the area graph knows nothing about
     * those.
     * </p>
     */
    private static Boolean accessibleThroughAreas(HasPosition from, HasPosition to) {
        try {
            if (atlantis.map.AMap.getMap() == null) return null;

            bwem.Area fromArea = atlantis.map.AMap.getMap()
                .getArea(new bwapi.WalkPosition(asTile(from).toWalkPosition()));
            bwem.Area toArea = atlantis.map.AMap.getMap()
                .getArea(new bwapi.WalkPosition(asTile(to).toWalkPosition()));

            if (fromArea == null || toArea == null) return null;
            if (fromArea.equals(toArea)) return null;

            return fromArea.isAccessibleFrom(toArea);
        } catch (Throwable t) {
            // Never let a model question break an order path: fall through to the
            // tile answer, which has no dependencies beyond our own grid.
            return null;
        }
    }

    /** A tile for the engine coordinate system, whichever position type we were handed. */
    private static bwapi.TilePosition asTile(HasPosition at) {
        if (at instanceof APosition) return ((APosition) at).toTilePosition();
        return at.position().p().toTilePosition();
    }

    public static boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
        if (source().canBuildHere(builder, building, at)) return true;

        // OpenBW's `Game.canBuildHere` is unreliable headless: it rejects tiles the
        // map data says are fine, which stopped the bot from placing its first
        // Pylon (measured 2026-10-08 on (3)TauCross1.1: "Can't find place for
        // Pylon", reason "Can't physically build here", while JBWeb's own tile grid
        // says the spot is placeable). Fall back to the map-derived answer, and only
        // when the source is the engine: the harness has its own rules and no map to
        // consult, so it keeps deciding for itself.
        if (!source().spawnsFromMapData()) return false;

        // JBWEB.isPlaceable reads its own usedGrid, which is only as current as the
        // unit lifecycle keeps it, so it can call an occupied tile free. The live
        // building list is the independent guard for THAT path.
        boolean jbwebUsable = JBWEB.isInitialized()
            && JBWEB.isPlaceable(building.ut(), at.toTilePosition());

        if (jbwebUsable && !BuildingTilesAreOccupied.check(at, building)) return true;

        // JBWEB refused, or is not available at all (the OpenBW case: JBWEB is a JNI
        // library whose natives do not exist on Linux, so InitJBWEB.init() fails and
        // AMap catches it and continues - _AI/LOCAL-STARCRAFT.md 187-189). Without
        // this branch every placement on OpenBW had no way left to say "yes": the
        // engine's composite canBuildHere (unreliable, see above) and the occupancy
        // guard (which can only say "not occupied"). That is why the bot could not
        // place its first Pylon.
        //
        // So answer from the ENGINE at tile level: for a building to stand at `at`,
        // every tile it covers must be walkable, buildable-including-buildings and
        // explored - the same three terms the command path enforces (see
        // tilesCoveredAreBuildable). The buildable term is the occupancy answer,
        // from the engine itself - which is
        // why this path does NOT also ask BuildingTilesAreOccupied: measured
        // 2026-10-08 on (3)TauCross1.1, that guard's tile-rectangle math reported
        // occupied=true for tiles the engine had just called buildable and empty
        // (buildable-including-buildings=true), so it turned every valid Pylon
        // position into a refusal. One source of truth per question.
        return tilesCoveredAreBuildable(building, at);
    }

    /**
     * Every tile the building would cover must be walkable, buildable and
     * <b>explored</b>, per the engine's own tile queries. Used when JBWEB cannot
     * answer (OpenBW); the building's top-left sits at {@code at}.
     *
     * <p>
     * {@code isBuildableIncludeBuildings()} is the engine's occupancy-aware answer,
     * so this covers "is something already standing here" without a second,
     * hand-rolled overlap test.
     * </p>
     *
     * <p>
     * <b>The explored term is not optional - it mirrors the engine exactly.</b> The
     * path a real build command takes is {@code Unit.build} -> {@code issueCommand}
     * -> {@code canIssueCommand} -> {@code Unit.canBuild(..., checkCanBuildHere=true,
     * ...)} -> {@code Game.canBuildHere(tile, type, unit, checkExplored=true)}, whose
     * per-tile loop is
     * {@code if (!isBuildable(x,y) || (checkExplored && !isExplored(x,y))) return false;}
     * (OpenBW {@code Templates::canBuildHere}). Without this term the placement
     * search accepts a footprint the command path will refuse, which is the measured
     * OpenBW failure: a Pylon was assigned to tile {@code [95,123]} - ~23 tiles from
     * the main and well outside the explored ring - the builder walked there and
     * {@code Unit.build} was rejected 190 times while the map grid said
     * {@code buildable:true}. The engine was answering {@code isExplored=false} to a
     * question the search never asked.
     * </p>
     */
    private static boolean tilesCoveredAreBuildable(AUnitType building, APosition at) {
        int left = at.tx();
        int top = at.ty();

        for (int x = left; x < left + building.getTilesWidth(); x++) {
            for (int y = top; y < top + building.getTilesHeights(); y++) {
                APosition tile = APosition.create(x, y);

                if (!tile.isWalkable()) return false;
                if (!tile.isBuildableIncludeBuildings()) return false;
                if (!tile.isExplored()) return false;
            }
        }

        return true;
    }
}