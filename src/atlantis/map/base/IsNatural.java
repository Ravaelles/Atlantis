package atlantis.map.base;

import atlantis.map.position.APosition;

public class IsNatural {
    public static boolean isPositionNatural(APosition position) {
        APosition natural = Bases.natural();

        if (natural == null || position == null) return false;

        return position.distTo(natural) <= 7;
    }
}
