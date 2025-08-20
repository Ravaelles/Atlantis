package atlantis.production.constructions.position.conditions.can_build_here;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.orders.requirements.AllowToProduceEarlyWithoutRequirements;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProtossAllowHereEarlyEvenWithoutRequirements {
    public static boolean allow(AUnit builder, AUnitType building, APosition position) {
        return AllowToProduceEarlyWithoutRequirements.isAllowedForProtoss(building)
            && allowAsProtoss(building, position);
    }

    private static boolean allowAsProtoss(AUnitType building, APosition position) {
//        if (building.isPylon()) return true;
//        if (A.supplyUsed() >= 20) return true;

        if (allowEarlyPlacementEvenWithoutFinishedPylon(building, position)) return true;

        if (building.needsPower() && !IsPoweredByAPylon.check(position)) return false;

        return isBuildable(position, 3, 2);
    }

    private static boolean allowEarlyPlacementEvenWithoutFinishedPylon(AUnitType building, APosition position) {
        return A.supplyTotal() <= 12
            && building.isGateway()
            && Select.ourWithUnfinished(AUnitType.Protoss_Pylon).inRadius(3.2, position).notEmpty()
            && A.println("Allow early Gateway - " + position.distToDigit(Select.ourWithUnfinished(AUnitType.Protoss_Pylon).inRadius(3.2, position).first()));
    }

    private static boolean isBuildable(APosition position, int dx, int dy) {
        for (int x = 0; x <= dx; x++) {
            for (int y = 0; y <= dy; y++) {
                if (!position.translateByTiles(x, y).isBuildableIncludeBuildings()) return false;
            }
        }

        return true;
    }
}
