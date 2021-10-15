package atlantis.util;

import atlantis.position.APosition;
import atlantis.units.AUnit;

public class Vectors {

    public static Vector fromPositionsBetween(AUnit unit1, AUnit unit2) {
        return fromPositionsBetween(unit1.getPosition(), unit2.getPosition());
    }

    public static Vector fromPositionsBetween(APosition p1, APosition p2) {
        return new Vector(p1.x - p2.x, p1.y - p2.y);
    }

    public static Vector vectorFromAngle(double angleInRadians, double radius) {
        return new Vector(
                radius * Math.cos(angleInRadians),
                radius * Math.sin(angleInRadians)
        );
    }

}
