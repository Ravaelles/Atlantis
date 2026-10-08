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
            return Atlantis.game().hasPath(from.position().p(), to.position().p());
        }

        @Override
        public boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
            return Atlantis.game().canBuildHere(at.toTilePosition(), building.ut(), builder.u());
        }
    };

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

        // Same shape as canBuildHere: OpenBW's pathing query answered "no path"
        // for every pair on (3)TauCross1.1, which left the natural base
        // undetermined and stopped the bot from expanding. The map-derived answer
        // applies only to the engine source, never to the harness.
        if (!source().spawnsFromMapData()) return false;
        if (!(to instanceof APosition)) return false;
        return JBWEB.isWalkable(((APosition) to).toTilePosition());
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

        // ...but the map-derived answer must not become a licence to build on top of
        // what is already there. JBWEB's grid only knows that once the unit
        // lifecycle keeps it current (Atlantis now calls onUnitDiscover/onUnitDestroy)
        // - and if that wiring is ever lost again, isPlaceable would hand back an
        // occupied tile. Asking the live unit list as well makes the fallback safe
        // on its own: a tile with a unit standing on it is never "placeable",
        // whoever answered first.
        if (BuildingTilesAreOccupied.check(at, building)) return false;

        // JBWEB.isPlaceable needs a live game (it reads Game.isBuildable and its own
        // grids). Where there is none - the stub world, and any caller before the map
        // is loaded - it would NPE, so the fallback stops here instead. The engine
        // answer above is all there is in that situation.
        if (!JBWEB.isInitialized()) return false;

        return JBWEB.isPlaceable(building.ut(), at.toTilePosition());
    }
}