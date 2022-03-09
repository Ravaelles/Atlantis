package atlantis.map.position;

import atlantis.Atlantis;
import atlantis.map.AMap;
import atlantis.util.Vector;

/**
 * This interface helps ease problems of overriding native bridge classes like e.g. BaseLocation which doesn't
 * have default constructor. Instead ABaseLocation can use this interface.
 */
public interface HasPosition {

    public static final int PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE = 30;

    APosition position();
    int x();
    int y();

    // === High-abstraction ========================================

    /**
     * Returns new position which is moved e.g. 15% in direction of the natural base (for bunker placement).
     */
    default APosition translatePercentTowards(HasPosition towards, int percentTowards) {
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

    default APosition makeWalkable(int maxRadius) {
        int currentRadius = 0;
        while (currentRadius <= maxRadius) {
            for (int dtx = -currentRadius; dtx <= currentRadius; dtx++) {
                for (int dty = -currentRadius; dty <= currentRadius; dty++) {
                    if (
                            dtx == -currentRadius || dtx == currentRadius
                                    || dty == -currentRadius || dty == currentRadius
                    ) {
                        APosition position = this.translateByTiles(dtx, dty);
                        if (position.isWalkable()) {
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

    default boolean distToLessThan(HasPosition otherPosition, double maxDist) {
        if (otherPosition == null) {
            return false;
        }

        return distTo(otherPosition) <= maxDist;
    }

    default boolean distToMoreThan(HasPosition otherPosition, double minDist) {
        if (otherPosition == null) {
            return false;
        }

        return distTo(otherPosition) >= minDist;
    }

    /**
     * Returns real ground distance to given point (not the air shortcut over impassable terrain).
     */
    default double groundDist(HasPosition other) {
        return PositionUtil.groundDistanceTo(this.position(), other.position());
    }

    default boolean isWalkable() {
        return Atlantis.game().isWalkable(position().toWalkPosition());
    }

    default boolean isExplored() {
        return Atlantis.game().isExplored(position().toTilePosition());
    }

    default boolean isPositionVisible() {
        return Atlantis.game().isVisible(position().toTilePosition());
    }

    default boolean isBuildable() {
        return Atlantis.game().isBuildable(position().toTilePosition());
    }

    default boolean isConnected() {
        return Atlantis.game().isVisible(position().toTilePosition());
    }

    default boolean hasPosition() {
        return position() != null;
    }

    // =========================================================

    default String toStringPixels() {
        return "(" + x() + ", " + y() + ")";
    }

    /**
     * Returns X coordinate in tiles, 1 tile = 32 pixels.
     */
    default int tx() {
        return x() / 32;
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
        return !position().makeValidFarFromBounds((int) (maxTilesAwayFromMapEdges * 32)).equals(position());
    }

    default boolean distToNearestChokeLessThan(double dist) {
        for (APosition center : AMap.allChokeCenters()) {
            if (center.distTo(this) <= dist) {
                return true;
            }
        }
        return false;
    }

}
