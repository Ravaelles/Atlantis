package atlantis.production.constructions.position.protoss;

import atlantis.Atlantis;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class IsPositionPowered {
    public static boolean isNotPowered(AUnitType building, APosition position) {
        if (true) return false; // Terribly broken for some reason. Not needed, CanBuildHere is enough.

        if (!We.protoss()) return false;
        if (!building.needsPower()) return false;

        return isNotPowered(position) && failed("No power");
    }

    private static boolean isNotPowered(APosition position) {
        return !Atlantis.game().hasPower(position.toTilePosition());

//        return Select.ourOfType(AUnitType.Protoss_Pylon).inRadius(6.8, position).empty();
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }
}
