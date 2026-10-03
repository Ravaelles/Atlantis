package atlantis.map;

import atlantis.Atlantis;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

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
        return source().hasPathBetween(from, to);
    }

    public static boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
        return source().canBuildHere(builder, building, at);
    }
}