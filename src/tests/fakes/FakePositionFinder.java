package tests.fakes;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.PositionFinder;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;

/**
 * The stub world's answers to the position-finder questions, moved out of the
 * {@code Env.isTesting()} branches that used to sit in {@code HasPosition}.
 * Same answers, one place: the origin is returned unchanged ("this one will
 * do"), which is what every caller already received in a test.
 */
public class FakePositionFinder implements PositionFinder.Source {
    public static void installAsSource() {
        PositionFinder.useSource(new FakePositionFinder());
    }

    @Override
    public APosition makeBuildable(HasPosition origin, int maxRadius) {
        return origin.position();
    }

    @Override
    public APosition ensureWithinBounds(HasPosition origin) {
        return origin.position();
    }

    @Override
    public APosition makeValidFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds) {
        return origin.position();
    }

    @Override
    public APosition makeBuildableFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds) {
        return origin.position();
    }

    @Override
    public APosition makeWalkable(HasPosition origin, int maxRadius, int step, ARegion sameRegion) {
        return origin.position();
    }

    @Override
    public APosition makeLandableFor(HasPosition origin, AUnit building) {
        return origin.position();
    }

    @Override
    public APosition makeWalkableAndFreeOfAnyGroundUnits(
        HasPosition origin, double maxRadius, double step, AUnit exceptUnit
    ) {
        return origin.position();
    }
}
