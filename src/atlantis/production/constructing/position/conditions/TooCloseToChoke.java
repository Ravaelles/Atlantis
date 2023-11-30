package atlantis.production.constructing.position.conditions;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;

public class TooCloseToChoke {
    public static boolean isTooCloseToChoke(AUnitType building, APosition position) {
        if (building.isBase()) return false;

        double minDist = building.isBunker() ? 1.3 : 3.8;

        for (AChoke choke : Chokes.chokes()) {
            if (choke.width() >= 5) {
                continue;
            }

            double distToChoke = choke.center().distTo(position) - choke.width();
            if (distToChoke <= minDist) {
                AbstractPositionFinder._CONDITION_THAT_FAILED = "Overlaps choke (" + distToChoke + ")";
                return true;
            }
        }

        return false;
    }
}