package atlantis.map;

import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.util.Vector;
import bwapi.Color;

import java.util.ArrayList;

public class ARegionBoundaryCalculator {

    public static ArrayList<ARegionBoundary> forRegion(ARegion region) {
        ArrayList<ARegionBoundary> boundaries = new ArrayList<>();
        APosition center = region.center();

        Vector vector = new Vector(96, 0);
        ARegionBoundary boundary;

        double angle = 0;
        while (angle < 2 * 3.14) {
            Vector rotated = vector.rotate(angle);
            createBoundaryFromVector(region, center, rotated, boundaries);

            angle += 0.14;
        }

        return boundaries;
    }

    private static void createBoundaryFromVector(ARegion region, APosition center, Vector vector, ArrayList<ARegionBoundary> boundaries) {
        APosition position;
        APosition lastBuildable = null;
//        APainter.paintCircleFilled(position, 4, Color.White);

        do {
            position = center.translateByVector(vector).makeValidFarFromBounds();

            if (position.isBuildable()) {
                lastBuildable = position;
            }

            vector = vector.addToLength(48);
        } while (position.isWalkable());

        if (lastBuildable != null) {
            boundaries.add(toBoundary(region, lastBuildable));
        }
    }

    private static ARegionBoundary toBoundary(ARegion region, APosition position) {
        return ARegionBoundary.create(region, position);
    }

//    private static void paintVector(Vector vector, APosition position) {
//        APainter.paintLine(position, (int) vector.x, (int) vector.y, Color.Teal);
//    }

}
