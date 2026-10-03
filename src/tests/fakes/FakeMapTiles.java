package tests.fakes;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;

/**
 * The stub world's answers to the tile questions, moved out of the
 * {@code Env.isTesting()} branches that used to sit in {@code HasPosition}. Same
 * answers, one place: everything is walkable and visible, buildable means "no unit
 * within 1.98 tiles", and explored is whatever the harness's flag says.
 */
public class FakeMapTiles implements MapTiles.Source {
    public static void installAsSource() {
        MapTiles.useSource(new FakeMapTiles());
    }

    @Override
    public boolean isWalkable(HasPosition at) {
        return true;
    }

    @Override
    public boolean isExplored(HasPosition at) {
        // Still the flag in APosition: tests/acceptance/BaseLocationsTest flips it
        // to ask for a position that has never been seen.
        return APosition.TESTING_EXPLORED;
    }

    @Override
    public boolean isVisible(HasPosition at) {
        return true;
    }

    @Override
    public boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
        return Select.all().countInRadius(1.98, at) == 0;
    }
}