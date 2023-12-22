package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class ForbiddenByStreetGridForBarracks {
    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!building.isBarracks()) return false;

        if (!streetGridMatches(position)) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Barracks grid doesn't allow it";
            return true;
        }

        return false;
    }

    private static boolean streetGridMatches(APosition position) {
        return position.tx() % 4 == 0 && position.ty() % 2 == 0;
    }
}