package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class TooCloseToChoke {
    public static boolean isTooCloseToChoke(AUnitType building, APosition position) {
        if (building.isBase()) return false;

        double minDist = minDist(building);

        for (AChoke choke : Chokes.chokes()) {
//            if (choke.width() >= 5) {
//                continue;
//            }

            double distToChoke = minDistToChoke(position, building, choke);
            if (distToChoke <= minDist && !isCbAndOnlyOneNearby(building, position, choke)) {
                return failed("Overlaps choke (" + distToChoke + ")");
            }
        }

        return false;
    }

    private static double minDist(AUnitType building) {
        double minDist = (building.isCombatBuilding())
            ? 1.5
            : (A.supplyUsed() >= 20 ? 4.7 : 2.3);

        if (We.protoss()) {
            if (building.isForge()) minDist = 1.9;
            if (A.supplyTotal() <= 10 && building.isPylon()) minDist = 1.2;
        }

        return minDist;
    }

    private static boolean isCbAndOnlyOneNearby(AUnitType building, APosition position, AChoke choke) {
        if (!building.isCombatBuilding()) return false;

        double minDistToChoke = minDistToChoke(position, building, choke);

        return ConstructionRequests.countExistingAndPlannedInRadius(building, minDistToChoke, choke) == 0;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._STATUS = reason;
        return true;
    }

    private static double minDistToChoke(APosition position, AUnitType building, AChoke choke) {
        if (building.isForge()) return 2.5;
        if (building.isCannon()) return 4.5;

        return choke.center().distTo(position);
    }
}