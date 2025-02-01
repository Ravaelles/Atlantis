package atlantis.production.constructions.position.conditions.can_build_here;

import atlantis.map.position.APosition;
import atlantis.production.orders.requirements.AllowToProduceEarlyWithoutRequirements;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class AllowHereEarlyEvenWithoutRequirements {
    public static boolean allowEarlyBuildingWithoutRequirements(AUnit builder, AUnitType building, APosition position) {
        return AllowToProduceEarlyWithoutRequirements.isAllowed(building)
            && allowAsProtoss(building, position);
    }

    private static boolean allowAsProtoss(AUnitType building, APosition position) {
        if (!We.protoss()) return true;
        if (building.isPylon()) return true;

        if (!IsPoweredByAPylon.check(position)) return false;

        return isBuildable(position, 3, 2);
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
