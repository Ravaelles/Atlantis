package tests.fakes;

import atlantis.map.MapTiles;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;

/**
 * The stub world's answers to the tile questions, moved out of the
 * {@code Env.isTesting()} branches that used to sit in {@code HasPosition}. Same
 * answers, one place: everything is walkable and visible, buildable means "no unit
 * within 1.98 tiles", and explored is whatever the harness's flag says.
 */
public class FakeMapTiles implements MapTiles.Source {
    /**
     * What {@link #isExplored} answers. Tests that need a position which has never
     * been seen set it to {@code false} for the length of their world;
     * {@code AbstractTestWithUnits.setUp()} puts it back, which is why one test
     * leaving it set can no longer decide the next test's answers.
     */
    public static boolean EXPLORED = false;

    public static void installAsSource() {
        MapTiles.useSource(new FakeMapTiles());
    }

    @Override
    public boolean isWalkable(HasPosition at) {
        return true;
    }

    @Override
    public boolean isExplored(HasPosition at) {
        return EXPLORED;
    }

    @Override
    public boolean isVisible(HasPosition at) {
        return true;
    }

    @Override
    public boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
        return Select.all().countInRadius(1.98, at) == 0;
    }

    @Override
    public boolean hasPathBetween(HasPosition from, HasPosition to) {
        // Everything is one connected map, which is what let tests place two
        // bases 40 tiles apart before this question had a name.
        return true;
    }
}