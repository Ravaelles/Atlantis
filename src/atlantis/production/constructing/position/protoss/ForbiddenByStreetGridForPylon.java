package atlantis.production.constructing.position.protoss;

import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class ForbiddenByStreetGridForPylon {
    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!building.isPylon()) return false;

        if (!streetGridMatches(position)) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Pylon street grid forbidden";
            return true;
        }

        return false;
    }

    private static boolean streetGridMatches(APosition position) {
        return position.tx() % ProtossForbiddenByStreetGrid.GRID_VALUE_X <= 1
            || position.ty() % ProtossForbiddenByStreetGrid.GRID_VALUE_Y <= 1;
//        return position.tx() % 4 <= 1 && position.ty() % 6 <= 1;
//        return position.tx() % 3 == 1 && position.ty() % 2 == 0;
//            && position.tx() % 12 != 6 && position.ty() % 8 != 4;
//            && position.tx() % 9 != 0 && position.ty() % 6 != 0;
    }
}
