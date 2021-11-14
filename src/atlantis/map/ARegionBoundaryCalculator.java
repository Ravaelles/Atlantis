package atlantis.map;

import atlantis.position.APosition;
import atlantis.util.Vector;

import java.util.ArrayList;

public class ARegionBoundaryCalculator {

    private static final double MIN_DIST_BETWEEN_POINTS = 1.8;
    private static APosition lastAdded = null;

    public static ArrayList<ARegionBoundary> forRegion(ARegion region) {
        ArrayList<ARegionBoundary> boundaries = new ArrayList<>();
        APosition center = region.center();
        Vector initVector = new Vector(96, 0);

        double angle = 0;
        while (angle < 2 * 3.14) {
            Vector rotated = initVector.rotate(angle);
            createBoundaryFromVector(region, center, rotated, boundaries);

            angle += 0.14;
        }

        return boundaries;
    }

    private static void createBoundaryFromVector(ARegion region, APosition center, Vector vector, ArrayList<ARegionBoundary> boundaries) {
        APosition position;
        APosition lastBuildable = null;

        do {
            position = center.translateByVector(vector).makeValidFarFromBounds();

            if (position.isBuildable()) {
                lastBuildable = position;
            }

            vector = vector.addLength(48);
        } while (position.isWalkable());

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
