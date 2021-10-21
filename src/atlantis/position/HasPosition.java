package atlantis.position;

import atlantis.units.AUnit;

/**
 * This interface helps ease problems of overriding native bridge classes like e.g. BaseLocation which doesn't
 * have public constructor. Instead ABaseLocation can use this interface.
 */
public interface HasPosition {
    APosition getPosition();
    int getX();
    int getY();

    default boolean distToLessThan(HasPosition otherPosition, double maxDist) {
        if (otherPosition == null) {
            return false;
        }

        return getPosition().distanceTo(otherPosition.getPosition()) <= maxDist;
    }

    default boolean distToMoreThan(HasPosition otherPosition, double minDist) {
        if (otherPosition == null) {
            return false;
        }

        return getPosition().distanceTo(otherPosition.getPosition()) >= minDist;
    }
}
