package atlantis.production.constructing.position.protoss;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.region.ARegion;
import atlantis.map.region.ARegionBoundary;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnitType;

public class ProtossTooCloseToRegionBoundaries {
    public static boolean isTooCloseToRegionBoundaries(AUnitType building, APosition position) {
        if (true) return false;

        if (!building.isPylon()) return false;

        ARegion region = position.region();
        if (region == null) return false;

        ARegionBoundary nearestBoundary = region.nearestBoundary(position);
        if (nearestBoundary == null) return false;

        int threshold = A.supplyTotal() <= 19 ? 6 : 3;
//        System.err.println("nearestBoundary.distTo(position) = " + nearestBoundary.distTo(position) + " / " + threshold);
        return nearestBoundary.distTo(position) <= threshold && failed("Too close to region boundary");
    }

    private static boolean failed(String reason) {
        AbstractPositionFinder._CONDITION_THAT_FAILED = reason;
        return true;
    }
}