package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class StreetGridOkayForSupplyDepot {
    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!building.isSupplyDepot()) return false;

        if (!streetGridMatches(position)) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = "Depot street grid doesn't match";
            return true;
        }

        return false;
//             otherSupplyDepotConstructionsAreNotClose(position);
    }

    private static boolean otherSupplyDepotConstructionsAreNotClose(APosition position) {
        return !ConstructionRequests.hasNotStartedNear(
            Terran_Supply_Depot, position, 5
        );
    }

    private static boolean streetGridMatches(APosition position) {
        return position.tx() % 3 == 0 && position.ty() % 2 == 0
            && position.tx() % 9 != 0 && position.ty() % 8 != 0;
    }
}


//        if (building.isSupplyDepot()) {
//            if (Select.ourOfTypeWithUnfinished(AUnitType.Terran_Supply_Depot).inRadius(2.5, position).notEmpty())
//            return false;
//            if (position.tx() % 3 > 0) return fail("TX Supply Depot modulo");
//            if (position.ty() % 3 > 0) return fail("TY Supply Depot modulo");
//            }
