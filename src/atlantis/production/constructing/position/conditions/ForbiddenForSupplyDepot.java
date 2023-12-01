package atlantis.production.constructing.position.conditions;

import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.We;

import static atlantis.units.AUnitType.Terran_Supply_Depot;

public class ForbiddenForSupplyDepot {
    public static boolean isForbidden(AUnit builder, AUnitType building, APosition position) {
        if (!We.terran() || !building.isSupplyDepot()) return false;

        return streetGridMatches(position)
            && otherSupplyDepotConstructionsAreNotClose(position);
    }

    private static boolean otherSupplyDepotConstructionsAreNotClose(APosition position) {
        return !ConstructionRequests.hasNotStartedNear(
            Terran_Supply_Depot, position, 20
        );
    }

    private static boolean streetGridMatches(APosition position) {
        return position.tx() % 2 == 1 || position.ty() % 2 == 1;
    }
}
