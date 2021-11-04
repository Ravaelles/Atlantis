package atlantis.position;

import atlantis.Atlantis;
import atlantis.map.AMap;
import bwapi.Point;
import bwapi.Position;

/**
 * This interface helps ease problems of overriding native bridge classes like e.g. BaseLocation which doesn't
 * have default constructor. Instead ABaseLocation can use this interface.
 */
public interface HasPosition {

    public static final int PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE = 20;

    APosition position();
    int x();
    int y();

    // === High-abstraction ========================================

    /**
     * Returns new position which is moved e.g. 15% in direction of the natural base (for bunker placement).
     */
    default APosition translatePercentTowards(HasPosition towards, int percentTowards) {
        return PositionHelper.getPositionMovedPercentTowards(
                this, towards, percentTowards
        );
    }

    /**
     * Returns new position which is moved e.g. 0.5 tiles towards <b>towards</b>.
     */
    default APosition translateTilesTowards(HasPosition towards, double tiles) {
        return PositionHelper.getPositionMovedTilesTowards(
                this, towards, tiles
        );
    }

    // =========================================================

    default double distTo(HasPosition position) {
        return PositionUtil.distanceTo(this, position);
    }

    default boolean distToLessThan(HasPosition otherPosition, double maxDist) {
        if (otherPosition == null) {
            return false;
        }

        return position().distTo(otherPosition.position()) <= maxDist;
    }

    default boolean distToMoreThan(HasPosition otherPosition, double minDist) {
        if (otherPosition == null) {
            return false;
        }

        return position().distTo(otherPosition.position()) >= minDist;
    }

    default boolean isWalkable() {
        return Atlantis.game().isWalkable(position().toWalkPosition());
    }

    default boolean isExplored() {
        return Atlantis.game().isExplored(position().toTilePosition());
    }

    default boolean isVisible() {
        return Atlantis.game().isVisible(position().toTilePosition());
    }

    default boolean isBuildable() {
        return Atlantis.game().isBuildable(position().toTilePosition());
    }

    default boolean isConnected() {
        return Atlantis.game().isVisible(position().toTilePosition());
    }

    // =========================================================

    /**
     * Returns X coordinate in tiles, 1 tile = 32 pixels.
     */
    default int getTileX() {
        return x() / 32;
    }

    /**
     * Returns Y coordinate in tiles, 1 tile = 32 pixels.
     */
    default int getTileY() {
        return y() / 32;
    }

    default LargeTile largeTile() {
        return new LargeTile(this);
    }

}
