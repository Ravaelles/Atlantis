package atlantis.map.region;

import atlantis.map.position.APosition;
import atlantis.util.Vector;

import java.util.ArrayList;

public class ARegionBoundaryCalculator {

    private static final double MIN_DIST_BETWEEN_POINTS = 1.8;
    private static APosition lastAdded = null;

    public static ArrayList<ARegionBoundary> forRegion(ARegion region) {
        ArrayList<ARegionBoundary> boundaries = new ArrayList<>();
//        if (A.isUms()) {
//            return boundaries;
//        }

        APosition center = region.center();
        Vector initVector = new Vector(64, 0);

        double angle = 0;
        while (angle < 2 * 3.14) {
            Vector rotated = initVector.rotate(angle);
            createBoundaryFromVector(region, center, rotated, boundaries);

            angle += 0.14;
        }

        return boundaries;
    }

    private static void createBoundaryFromVector(ARegion region, APosition center, Vector vector, ArrayList<ARegionBoundary> boundaries) {
        if (center == null) {
//            System.err.println("Invalid region center NULL");
//            System.err.println(region);
            return;
        }

        APosition position;
        APosition lastBuildable = null;

        do {
            position = center.translateByVector(vector).makeBuildableGroundPositionFarFromBounds();

            if (position != null && position.isBuildableIncludeBuildings()) {
                lastBuildable = position;
            }

            vector = vector.addLength(48);
        } while (position != null && position.isWalkable() && vector.length() <= 30 * 32);

        if (lastBuildable != null) {
            if (lastAdded == null || lastAdded.distToMoreThan(lastBuildable, MIN_DIST_BETWEEN_POINTS)) {
                boundaries.add(toBoundary(region, lastBuildable));
                lastAdded = lastBuildable;
            }
        }
    }

    private static ARegionBoundary toBoundary(ARegion region, APosition position) {
        return ARegionBoundary.create(region, position);
    }

}
