package atlantis.map.base.define;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;

public class BaseLocationsNearestTo {
    public static Positions<ABaseLocation> takeN(int n, HasPosition nearestTo) {
        Positions<ABaseLocation> baseLocations = new Positions<>();

        for (ABaseLocation baseLocation : BaseLocations.baseLocations()) {
            double dist = nearestTo.groundDist(baseLocation);

            if (dist <= 10) continue;

            baseLocations.addPosition(baseLocation);
        }

        return baseLocations.sortByGroundDistanceTo(nearestTo, true).limit(n);
    }

//    public static HasPosition closestTo(HasPosition position) {
//        double bestDist = 9999;
//        ABaseLocation bestBase = null;
//
//        for (ABaseLocation baseLocation : BaseLocations.baseLocations()) {
//            double dist = position.groundDist(baseLocation);
//
//            if (dist <= 10) continue;
//
//            if (dist < bestDist) {
//                bestDist = dist;
//                bestBase = baseLocation;
//            }
//        }
//
//        return bestBase;
//    }
}
