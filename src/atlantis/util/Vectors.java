package atlantis.util;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class Vectors {

    public static Vector fromPositionsBetween(AUnit unit1, AUnit unit2) {
        return fromPositionsBetween(unit2.position(), unit1.position());
    }

    public static Vector fromPositionsBetween(APosition p1, APosition p2) {
        return new Vector(p2.x - p1.x, p2.y - p1.y);
    }

    public static Vector vectorFromAngle(double angleInRadians, double radius) {
        return new Vector(
            radius * Math.cos(angleInRadians),
            radius * Math.sin(angleInRadians)
        );
    }
}
