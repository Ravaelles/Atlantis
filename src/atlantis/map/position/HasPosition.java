package atlantis.map.position;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Vector;
import bwapi.Color;
import bwapi.Position;

/**
 * This interface helps ease problems of overriding native bridge classes like e.g. BaseLocation which doesn't
 * have default constructor. Instead ABaseLocation can use this interface.
 */
public interface HasPosition {
    public static final int PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE = 32;

    APosition position();

    int x();

    int y();

    // === High-abstraction ========================================

    /**
     * Returns new position which is moved e.g. 15% in direction of the natural base (for bunker placement).
     */
    default APosition translatePercentTowards(HasPosition towards, int percentTowards) {
        if (towards == null) return position();

        return translatePercentTowards(percentTowards, towards);
    }

    default APosition translatePercentTowards(int percentTowards, HasPosition towards) {
        return PositionHelper.getPositionMovedPercentTowards(
            this, towards, percentTowards
        );
    }

    /**
     * Returns new position which is moved e.g. 0.5 tiles towards <b>towards</b>.
     */
    default APosition translateTilesTowards(HasPosition towards, double tiles) {
        return translateTilesTowards(tiles, towards);
    }

    default APosition translateTilesTowards(double tiles, HasPosition towards) {
        return PositionHelper.getPositionMovedTilesTowards(
            this, towards, tiles
        );
    }

    /**
     * Returns new position object that is translated in [x,y] pixels.
     */
    default APosition translateByPixels(int pixelDX, int pixelDY) {
        return new APosition(x() + pixelDX, y() + pixelDY);
    }

    /**
     * Returns new position object that is translated in [x,y] tiles.
     */
    default APosition translateByTiles(int tileDX, int tileDY) {
        return new APosition(x() + tileDX * 32, y() + tileDY * 32);
    }

    default APosition translateByTiles(double tileDX, double tileDY) {
        return new APosition((int) (x() + tileDX * 32), (int) (y() + tileDY * 32));
    }

    default APosition translateByVector(Vector vector) {
        return new APosition((int) (x() + vector.x), (int) (y() + vector.y));
    }

    default APosition makeBuildable(int maxRadius) {
        if (Env.isTesting()) return position();

        APosition position = this.position();
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
                        position = this.translateByTiles(dtx, dty);
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

    default APosition makeBuildableFarFromBounds(int atLeastTilesAwayFromBounds) {
        if (Env.isTesting()) return position();

        APosition position = this.position();
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
                        position = this.translateByTiles(dtx, dty);
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

    default APosition makeWalkable(int maxRadius, ARegion sameRegion) {
        if (Env.isTesting()) return position();

        APosition position = this.position();
        if (position.isWalkable()) {
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
                        position = this.translateByTiles(dtx, dty);
                        if (position.isWalkable() && (sameRegion == null || sameRegion.equals(position.region()))) {
                            return position;
                        }
                    }
                }
            }

            currentRadius++;
        }

        return null;
    }

    default APosition makeLandableFor(AUnit building) {
        if (Env.isTesting()) return position();

        int currentRadius = 0;
        int maxRadius = 8;
        while (currentRadius <= maxRadius) {
            for (int dtx = -currentRadius; dtx <= currentRadius; dtx++) {
                for (int dty = -currentRadius; dty <= currentRadius; dty++) {
                    if (
                        dtx == -currentRadius || dtx == currentRadius
                            || dty == -currentRadius || dty == currentRadius
                    ) {
                        APosition position = this.translateByTiles(dtx, dty);
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

//    default APosition makeFreeOfOurUnits(int maxRadius, double checkMargin, AUnit exceptUnit) {
//        int currentRadius = 0;
//        while (currentRadius <= maxRadius) {
//            for (int dtx = -currentRadius; dtx <= currentRadius; dtx++) {
//                for (int dty = -currentRadius; dty <= currentRadius; dty++) {
//                    if (
//                            dtx == -currentRadius || dtx == currentRadius
//                                    || dty == -currentRadius || dty == currentRadius
//                    ) {
//                        APosition position = this.translateByTiles(dtx, dty);
//                        if (
//                            position.isWalkable()
//                                && Select.our().exclude(exceptUnit).inRadius(checkMargin, position).empty()
//                        ) {
//                            return position;
//                        }
//                    }
//                }
//            }
//
//            currentRadius++;
//        }
//
//        return null;
//    }

    default APosition makeWalkableAndFreeOfAnyGroundUnits(double maxRadius, double step, AUnit exceptUnit) {
        if (Env.isTesting()) return position();

        double currentRadius = 0;
        double closenessMargin = 0.1;
        Selection our = Select.our().groundUnits().inRadius(maxRadius + 1, this).exclude(exceptUnit);

        while (currentRadius <= maxRadius) {
            for (double dtx = -currentRadius; dtx <= currentRadius; dtx += step) {
                for (double dty = -currentRadius; dty <= currentRadius; dty += step) {
                    if (
                        dtx == -currentRadius || dtx == currentRadius
                            || dty == -currentRadius || dty == currentRadius
                    ) {
                        APosition position = this.translateByTiles(dtx, dty);
                        if (
                            position.isWalkable()
                                && our.inRadius(closenessMargin, position).empty()
                        ) {
//                            System.err.println("position = " + position + " / " + position.distTo(exceptUnit));
                            return position;
                        }
                    }
                }
            }

            currentRadius++;
        }

        return null;
    }

    // =========================================================

    default double distTo(HasPosition position) {
        return PositionUtil.distanceTo(this, position);
    }

    default double distToOr999(HasPosition position) {
        if (position == null) return 999;

        return PositionUtil.distanceTo(this, position);
    }

    default double distToOrMinus1(HasPosition position) {
        if (position == null) return -1;

        return PositionUtil.distanceTo(this, position);
    }

    default boolean distToLessThan(HasPosition otherPosition, double maxDist) {
        if (otherPosition == null) return false;

        return distTo(otherPosition) <= maxDist;
    }

    default boolean distToMoreThan(HasPosition otherPosition, double minDist) {
        if (otherPosition == null) return false;

        return distTo(otherPosition) >= minDist;
    }

    default double distToMain() {
        AUnit base = Select.main();
        if (base == null) return 999;

        return base.distTo(this);
    }

    default double groundDistToMain() {
        AUnit base = Select.main();
        if (base == null) return 999;

        return base.groundDist(this);
    }

    /**
     * Returns real ground distance to given point (not the air shortcut over impassable terrain).
     */
    default double groundDist(HasPosition other) {
        return PositionUtil.groundDistanceTo(this.position(), other.position());
    }

    default boolean isWalkable() {
        if (Env.isTesting()) return true;

        return Atlantis.game().isWalkable(position().p().toWalkPosition());
    }

    default boolean isExplored() {
        if (Env.isTesting()) return APosition.TESTING_EXPLORED;

        return Atlantis.game().isExplored(position().p().toTilePosition());
    }

    default boolean isPositionVisible() {
        if (Env.isTesting()) return true;

        return Atlantis.game().isVisible(position().p().toTilePosition());
    }

    default boolean isBuildableNotIncludingBuildings() {
        if (Env.isTesting()) return Select.all().countInRadius(1.98, this) == 0;

        return Atlantis.game().isBuildable(position().p().toTilePosition());
    }

    default boolean isBuildableIncludeBuildings() {
        if (Env.isTesting()) return Select.all().countInRadius(1.98, this) == 0;

        return Atlantis.game().isBuildable(tx(), ty(), true);
    }

    default boolean isConnected() {
        APosition position = position();
        if (position == null) return false;

        Position p = position.p();
        if (p == null) return false;

        return Atlantis.game().isVisible(p.toTilePosition());
    }

    default boolean hasPosition() {
        return position() != null && position().x() > 0 && position().x() < 32000;
    }

    // =========================================================

    default String toStringPixels() {
        return "(px:" + x() + ",py:" + y() + ")";
    }

    /**
     * Returns X coordinate in tiles, 1 tile = 32 pixels.
     */
    default int tx() {
        return x() / 32;
    }

    default double txWithPrecision() {
        return x() / 32.0;
    }

    /**
     * Returns Y coordinate in tiles, 1 tile = 32 pixels.
     */
    default int ty() {
        return y() / 32;
    }

    default LargeTile largeTile() {
        return new LargeTile(this);
    }

    default boolean nearMapEdge(double maxTilesAwayFromMapEdges) {
        return !position().makeBuildableFarFromBounds((int) (maxTilesAwayFromMapEdges * 32)).equals(position());
    }

    default boolean distToNearestChokeLessThan(double dist) {
        for (AChoke choke : Chokes.chokes()) {
            if (choke.width() <= 5 && choke.center().distTo(this) <= dist) {
                return true;
            }
        }
//        for (APosition center : AMap.allChokeCenters()) {
//            if (center.distTo(this) <= dist) {
//                return true;
//            }
//        }
        return false;
    }

    default boolean regionsMatchOrClose(HasPosition other, double maxGroundDist) {
        if (regionsMatch(other)) return true;

        return groundDist(other) <= maxGroundDist;
    }

    default boolean regionsMatch(HasPosition other) {
        if (other == null || other.position() == null || !other.hasPosition() || other.position() == null) return false;
        if (!hasPosition()) return false;

        ARegion region = position().region();

        if (region == null || region.position() == null || !region.hasPosition()) return false;

        return region.equals(other.position().region());
    }

    static HasPosition nearestPositionFreeFromUnits(Positions<HasPosition> points, AUnit nearestTo) {
        for (HasPosition position : points.sortByDistanceTo(nearestTo, true).list()) {
            if (Select.our().exclude(nearestTo).inRadius(0.3, position).empty()) {
                return position;
            }
        }

        return null;
    }

    default void paintCircleFilled(int radius, Color color) {
        AAdvancedPainter.paintCircleFilled(this, radius, color);
    }

    default void paintCircle(int radius, Color color) {
        AAdvancedPainter.paintCircle(this, radius, color);
    }

    default void paintCircle(int radius, int width, Color color) {
        for (int i = radius; i <= radius + width; i++) {
            AAdvancedPainter.paintCircle(this, i, color);
        }
    }

    default void paintRectangle(double width, double height, Color color) {
        AAdvancedPainter.paintRectangle(
            this.translateByTiles(-width / 2, -height / 2), (int) (width * 32), (int) (height * 32), color
        );
    }

    default void paintLine(HasPosition to, Color color) {
        AAdvancedPainter.paintLine(this, to, color);
    }

    default void paintLineDouble(HasPosition to, Color color) {
        AAdvancedPainter.paintLine(this, to, color);
        AAdvancedPainter.paintLine(this.translateByPixels(1, 1), to.translateByPixels(1, 1), color);
    }

    default void paintTextCentered(HasPosition position, String text, Color color) {
        AAdvancedPainter.paintTextCentered(position, text, color, false);
    }

    default void paintTextCentered(String text, Color color) {
        AAdvancedPainter.paintTextCentered(this, text, color, false);
    }

    default void paintTextCentered(String text, Color color, int tyOffset) {
        AAdvancedPainter.paintTextCentered(this.translateByTiles(0, tyOffset), text, color, false);
    }

    default String distToDigit(HasPosition to) {
        return "(" + A.digit(distTo(to)) + ")";
    }

    default boolean equals(HasPosition obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.position() == null) return false;

        int otherX = obj.x();
        int otherY = obj.y();
        return this.x() == otherX && this.y() == otherY;
    }

    default int compareTo(HasPosition o) {
        int compare = Integer.compare(x(), y());
        if (compare == 0) {
            compare = Integer.compare(x(), y());
        }
        return compare;
    }


    default boolean isCloseToMapBounds() {
        int px = x();
        int py = y();

        if (px < PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE) return true;
        else if (px >= (32 * AMap.getMapWidthInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE)) return true;

        if (py < PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE) return true;
        else return py >= (32 * AMap.getMapHeightInTiles() - PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE);
    }

    default boolean isCloseToMapBounds(int tilesAwayFromEdge) {
        int px = x();
        int py = y();
        int allowedPixelsAway = tilesAwayFromEdge * 32;

        if (px <= allowedPixelsAway) return true;
        else if (px >= (32 * AMap.getMapWidthInTiles() - allowedPixelsAway)) return true;

        if (py <= allowedPixelsAway) return true;
        else return py >= (32 * AMap.getMapHeightInTiles() - allowedPixelsAway);
    }

    default double distToMapBorders() {
        return Math.min(
            Math.min(tx(), ty()),
            Math.min((AMap.getMapWidthInTiles() - tx()), (AMap.getMapHeightInTiles() - ty()))
        );
    }
}
