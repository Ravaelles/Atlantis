package atlantis.map;

import atlantis.Atlantis;
import atlantis.map.position.HasPosition;

/**
 * What the map knows about a single tile: is it walkable, has it been explored,
 * is it visible now, can a building stand there.
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
}