package atlantis.map.position;

import atlantis.map.region.ARegion;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Where to stand when a position must satisfy a map predicate: buildable,
 * walkable, far from the bounds, landable, free of units.
 *
 * <p>In a game these answers come from spiral searches over the engine's tile
 * data. The stub world has no map behind it, so the harness answers "this one
 * will do" - and until now it did so from inside production code: every method
 * below wrote its own {@code if (Env.isTesting()) return position()} branch
 * in front of the search, seven times over in {@code HasPosition} alone. A
 * port says which of the two is in charge, so the branches live in one place
 * and the fake world gets to change its mind per question.</p>
 *
 * <p>The answers are byte-for-byte the ones the branches gave: in a test the
 * origin is returned unchanged, which is what every caller already received.</p>
 */
public class PositionFinder {
    public interface Source {
        APosition makeBuildable(HasPosition origin, int maxRadius);

        APosition ensureWithinBounds(HasPosition origin);

        APosition makeValidFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds);

        APosition makeBuildableFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds);

        APosition makeWalkable(HasPosition origin, int maxRadius, int step, ARegion sameRegion);

        APosition makeLandableFor(HasPosition origin, AUnit building);

        APosition makeWalkableAndFreeOfAnyGroundUnits(
            HasPosition origin, double maxRadius, double step, AUnit exceptUnit
        );
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
        public APosition makeBuildable(HasPosition origin, int maxRadius) {
            APosition position = origin.position();
            if (position.isBuildableIncludeBuildings()) {
                return position;
            }

            int currentRadius = 0;
            while (currentRadius <= maxRadius) {
                for (int dtx = -currentRadius; dtx <= currentRadius; dtx++) {
                    for (int dty = -currentRadius; dty <= currentRadius; dty++) {
                        if (
                            dtx == -currentRadius || dtx == currentRadius
                                || dty == -currentRadius || dty == currentRadius
                        ) {
                            position = origin.translateByTiles(dtx, dty);
                            if (position.isBuildableIncludeBuildings()) {
                                return position;
                            }
                        }
                    }
                }

                currentRadius++;
            }

            return null;
        }

        @Override
        public APosition ensureWithinBounds(HasPosition origin) {
            APosition position = origin.position();

            if (!position.isCloseToMapBounds(0)) {
                return position;
            }

            int atLeastTilesAwayFromBounds = 0;
            int currentRadius = 0;
            while (currentRadius <= 8) {
                for (int dtx = -currentRadius; dtx <= currentRadius; dtx += 2) {
                    for (int dty = -currentRadius; dty <= currentRadius; dty += 2) {
                        if (
                            dtx == -currentRadius || dtx == currentRadius
                                || dty == -currentRadius || dty == currentRadius
                        ) {
                            position = origin.translateByTiles(dtx, dty);
                            if (!position.isCloseToMapBounds(atLeastTilesAwayFromBounds)) {
                                return position;
                            }
                        }
                    }
                }

                currentRadius += 2;
            }

            return null;
        }

        @Override
        public APosition makeValidFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds) {
            if (!origin.position().isCloseToMapBounds(atLeastTilesAwayFromBounds)) {
                return origin.position();
            }

            int currentRadius = 0;
            while (currentRadius <= atLeastTilesAwayFromBounds) {
                for (int dtx = -currentRadius; dtx <= currentRadius; dtx++) {
                    for (int dty = -currentRadius; dty <= currentRadius; dty++) {
                        if (
                            dtx == -currentRadius || dtx == currentRadius
                                || dty == -currentRadius || dty == currentRadius
                        ) {
                            APosition position = origin.translateByTiles(dtx, dty);
                            if (!position.isCloseToMapBounds(atLeastTilesAwayFromBounds)) {
                                return position;
                            }
                        }
                    }
                }

                currentRadius++;
            }

            return null;
        }

        @Override
        public APosition makeBuildableFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds) {
            APosition position = origin.position();
            if (position.isBuildableIncludeBuildings()) {
                return position;
            }

            int currentRadius = 0;
            while (currentRadius <= atLeastTilesAwayFromBounds) {
                for (int dtx = -currentRadius; dtx <= currentRadius; dtx++) {
                    for (int dty = -currentRadius; dty <= currentRadius; dty++) {
                        if (
                            dtx == -currentRadius || dtx == currentRadius
                                || dty == -currentRadius || dty == currentRadius
                        ) {
                            position = origin.translateByTiles(dtx, dty);
                            if (position.isBuildableIncludeBuildings() && !position.isCloseToMapBounds(atLeastTilesAwayFromBounds)) {
                                return position;
                            }
                        }
                    }
                }

                currentRadius++;
            }

            return null;
        }

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
                            if (position.isWalkable() && (sameRegion == null || sameRegion.equals(position.region()))) {
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
            int currentRadius = 0;
            int maxRadius = 8;
            while (currentRadius <= maxRadius) {
                for (int dtx = -currentRadius; dtx <= currentRadius; dtx++) {
                    for (int dty = -currentRadius; dty <= currentRadius; dty++) {
                        if (
                            dtx == -currentRadius || dtx == currentRadius
                                || dty == -currentRadius || dty == currentRadius
                        ) {
                            APosition position = origin.translateByTiles(dtx, dty);
                            if (!position.isExplored() || building.u().canLand(position.p().toTilePosition())) {
                                return position;
                            }
                        }
                    }
                }

                currentRadius++;
            }

            return null;
        }

        @Override
        public APosition makeWalkableAndFreeOfAnyGroundUnits(
            HasPosition origin, double maxRadius, double step, AUnit exceptUnit
        ) {
            double currentRadius = 0;
            double closenessMargin = 0.1;
            Selection our = Select.our().groundUnits().inRadius(maxRadius + 1, origin).exclude(exceptUnit);

            while (currentRadius <= maxRadius) {
                for (double dtx = -currentRadius; dtx <= currentRadius; dtx += step) {
                    for (double dty = -currentRadius; dty <= currentRadius; dty += step) {
                        if (
                            dtx == -currentRadius || dtx == currentRadius
                                || dty == -currentRadius || dty == currentRadius
                        ) {
                            APosition position = origin.translateByTiles(dtx, dty);
                            if (
                                position.isWalkable()
                                    && our.inRadius(closenessMargin, position).empty()
                            ) {
                                return position;
                            }
                        }
                    }
                }

                currentRadius++;
            }

            return null;
        }
    };

    // =========================================================

    public static APosition makeBuildable(HasPosition origin, int maxRadius) {
        return source().makeBuildable(origin, maxRadius);
    }

    public static APosition ensureWithinBounds(HasPosition origin) {
        return source().ensureWithinBounds(origin);
    }

    public static APosition makeValidFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds) {
        return source().makeValidFarFromBounds(origin, atLeastTilesAwayFromBounds);
    }

    public static APosition makeBuildableFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds) {
        return source().makeBuildableFarFromBounds(origin, atLeastTilesAwayFromBounds);
    }

    public static APosition makeWalkable(HasPosition origin, int maxRadius, int step, ARegion sameRegion) {
        return source().makeWalkable(origin, maxRadius, step, sameRegion);
    }

    public static APosition makeLandableFor(HasPosition origin, AUnit building) {
        return source().makeLandableFor(origin, building);
    }

    public static APosition makeWalkableAndFreeOfAnyGroundUnits(
        HasPosition origin, double maxRadius, double step, AUnit exceptUnit
    ) {
        return source().makeWalkableAndFreeOfAnyGroundUnits(origin, maxRadius, step, exceptUnit);
    }
}
