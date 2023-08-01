package atlantis.production.constructing.position;

import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;

public class TooCloseToRegionBoundaries {
    public static boolean isTooCloseToRegionBoundaries(APosition position) {
        ARegion region = position.region();
        if (region == null) {
            return false;
        }

        ARegionBoundary nearestBoundary = region.nearestBoundary(position);
        if (nearestBoundary == null) {
            return false;
        }

        return nearestBoundary.distTo(position) <= 2.5;
    }
}