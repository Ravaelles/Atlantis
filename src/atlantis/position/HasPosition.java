package atlantis.position;

import atlantis.map.AMap;

/**
 * This interface helps ease problems of overriding native bridge classes like e.g. BaseLocation which doesn't
 * have default constructor. Instead ABaseLocation can use this interface.
 */
public interface HasPosition {

    public static final int PIXELS_TO_MAP_BOUNDARIES_CONSIDERED_CLOSE = 20;

    APosition getPosition();
    int x();
    int y();

    default boolean distToLessThan(HasPosition otherPosition, double maxDist) {
        if (otherPosition == null) {
            return false;
        }

        return getPosition().distTo(otherPosition.getPosition()) <= maxDist;
    }

    default boolean distToMoreThan(HasPosition otherPosition, double minDist) {
        if (otherPosition == null) {
            return false;
        }

        return getPosition().distTo(otherPosition.getPosition()) >= minDist;
    }


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

    default boolean isVisible() {
        return AMap.isVisible(getPosition());
    }

}
