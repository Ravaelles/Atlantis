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

    private static boolean regionMatches(ARegion sameRegion, APosition position) {
        if (sameRegion == null) return true;

        try {
            return sameRegion.equals(position.region());
        } catch (Exception | Error ignored) {
            return true;
        }
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

    /**
     * Real spiral over walkability, not identity (owner 2026-10-24):
     * the identity shortcut blinded every sim that needed a slide -
     * GhostAdvance had to reimplement the spiral locally to pin
     * wall-sliding at all. With all-walkable stubs this answers
     * exactly as before (origin passes the first check); with an
     * arena-backed MapTiles source it actually searches.
     */
    @Override
    public APosition makeWalkable(HasPosition origin, int maxRadius, int step, ARegion sameRegion) {
        APosition position = origin.position();
        if (position.isWalkable()) {
            return position;
        }

        int currentRadius = 0;
        while (currentRadius <= maxRadius) {
            for (int dtx = -currentRadius; dtx <= currentRadius; dtx += step) {
                for (int dty = -currentRadius; dty <= currentRadius; dty += step) {
                    if (
                        dtx == -currentRadius || dtx == currentRadius
                            || dty == -currentRadius || dty == currentRadius
                    ) {
                        position = origin.translateByTiles(dtx, dty);
                        if (position.isWalkable() && regionMatches(sameRegion, position)) {
                            return position;
                        }
                    }
                }
            }

            currentRadius += step;
        }

        return null;
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
