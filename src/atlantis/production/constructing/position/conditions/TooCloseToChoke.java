package atlantis.production.constructing.position.conditions;

import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class TooCloseToChoke {
    public static boolean isTooCloseToChoke(AUnitType building, APosition position) {
        if (building.isBase()) return false;

        double minDist = (building.isCombatBuilding())
            ? 1.5
            : (A.supplyUsed() >= 20 ? 4.7 : 2.3);

        if (We.protoss() && building.isForge()) minDist = 1.5;

        for (AChoke choke : Chokes.chokes()) {
//            if (choke.width() >= 5) {
//                continue;
//            }

            double distToChoke = minDistToChoke(position, choke);
            if (distToChoke <= minDist) {
                return failed("Overlaps choke (" + distToChoke + ")");
            }
        }

        return false;
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }

    private static double minDistToChoke(APosition position, AChoke choke) {
        return choke.center().distTo(position);
    }
}