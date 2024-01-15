package atlantis.production.constructing.position.protoss;

import atlantis.Atlantis;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class IsPositionPowered {
    public static boolean isNotPowered(AUnitType building, APosition position) {
        if (!We.protoss()) return false;
        if (!building.needsPower()) return false;

        return isNotPowered(position) ? failed("No power") : false;
    }

    private static boolean isNotPowered(APosition position) {
        return !Atlantis.game().hasPower(position.toTilePosition());
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}
