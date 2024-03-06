package atlantis.production.constructing.position.terran;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class ForbiddenByStreetGridForSupplyDepotAndAcademy {
    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
//        if (true) return false;
//        if (A.supplyTotal() <= 20) return false;

        if (!building.isSupplyDepot() && !building.isAcademy()) return false;

        int modulo;
        if ((modulo = (position.tx()) % 2) != 0) return failed("TX modulo M = " + modulo);
        if ((modulo = (position.ty()) % 2) != 0) return failed("TY modulo N = " + modulo);

        if ((modulo = (position.tx()) % 9) >= 5) return failed("TX modulo M1 = " + modulo);
        if ((modulo = (position.ty()) % 9) >= 5) return failed("TY modulo N1 = " + modulo);

        if (!streetGridMatches(position)) {
            AbstractPositionFinder._CONDITION_THAT_FAILED = building.name() + " street grid doesn't allow it";
            return true;
        }

        return otherSupplyDepotConstructionsAreTooClose(position);
    }

    private static boolean otherSupplyDepotConstructionsAreTooClose(APosition position) {
        return ConstructionRequests.hasNotStartedNear(
            Terran_Supply_Depot, position, 5
        );
    }

    private static boolean streetGridMatches(APosition position) {
        return position.tx() % 12 != 1 && position.ty() % 8 != 1;
//        return position.tx() % 3 == 1 && position.ty() % 2 == 0;
//            && position.tx() % 12 != 6 && position.ty() % 8 != 4;
//            && position.tx() % 9 != 0 && position.ty() % 6 != 0;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}
