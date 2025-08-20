package atlantis.map.position.helpers;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.map.region.ARegion;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;
import bwapi.Color;

public class NearestWalkable {
    public static APosition to(
        HasPosition walkableAroundPosition, HasPosition nearestTo, boolean freeFromUnits
    ) {
        nearestTo = findPointThatBelongsToDestinationRegionNearestToSource(nearestTo, walkableAroundPosition);

        return defineWalkableFreeFromEnemiesInDistOf(
            walkableAroundPosition, 0, 20, 2,
            null, -1,
            freeFromUnits
        ).nearestTo(nearestTo);
    }


    public static APosition andFreeFromEnemies(
        HasPosition walkableAroundPosition, HasPosition nearestTo, int minDist, int maxDist, int step,
        Selection enemies, int noEnemiesInRadius
    ) {
        return defineWalkableFreeFromEnemiesInDistOf(
            walkableAroundPosition, minDist, maxDist, step,
            enemies, noEnemiesInRadius,
            false
        ).groundNearestTo(nearestTo);
    }

    // =========================================================

    private static HasPosition findPointThatBelongsToDestinationRegionNearestToSource(
        HasPosition source, HasPosition target
    ) {
        if (target == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Target null for target " + target + " / " + source);
            return source;
        }
        if (target.position() == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Target position null for target " + target + " / " + source);
            return source;
        }

        ARegion targetRegion = target.position().region();
        if (targetRegion == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Very bad case, region null between " + source + " and " + target);
            return target;
        }

        APosition approximation;
        int steps = 10;
        for (int i = 0; i <= steps; i++) {
            approximation = source.translatePercentTowards(target, 10 * i);
            if (approximation.regionsMatch(targetRegion)) {
                return approximation;
            }
        }

        return target;
    }

    private static Positions<APosition> defineWalkableFreeFromEnemiesInDistOf(
        HasPosition walkableAroundPosition, int minDist, int maxDist, int step,
        Selection enemiesOrNull, int noEnemiesInRadius,
        boolean freeFromUnits
    ) {
        double currentRadius = minDist;
        Positions<APosition> positions = new Positions<>();

        int smallStep = A.inRange(2,maxDist - minDist, 3);

        while (currentRadius <= maxDist) {
            for (double dtx = -currentRadius; dtx <= currentRadius; dtx += smallStep) {
                for (double dty = -currentRadius; dty <= currentRadius; dty += smallStep) {
                    if (
                        dtx == -currentRadius || dtx == currentRadius
                            || dty == -currentRadius || dty == currentRadius
                    ) {
//                        counter++;
                        APosition position = walkableAroundPosition.translateByTiles(dtx, dty);
//                        position.paintCircle(12, Color.Grey);
                        if (position.isWalkable()) {
//                            System.err.println("position = " + position + " / dist=" + position.groundDist(walkableAroundPosition));
//                            return position;
                            double dist = position.groundDist(walkableAroundPosition);
                            if (
                                dist >= minDist
                                    && dist <= maxDist
                                    && position.hasPathTo(walkableAroundPosition)
                                    && freeFromEnemies(enemiesOrNull, noEnemiesInRadius, position)
                                    && freeFromUnits(freeFromUnits, position)
                            ) {
//                                System.err.println("OK added");
                                positions.addPosition(position);
                                position.paintCircleFilled(10, Color.Green);
                            }
                        }
                    }
                }
            }

            currentRadius += step;
        }

//        if (positions.size() > 0) GameSpeed.pauseGame();

        return positions;
    }

    private static boolean freeFromUnits(boolean freeFromUnits, APosition position) {
        if (!freeFromUnits) return true;

        return Select.all().groundUnits().countInRadius(0.5, position) == 0;
    }

    private static boolean freeFromEnemies(Selection enemiesOrNull, int noEnemiesInRadius, APosition position) {
        return enemiesOrNull == null
            || enemiesOrNull.countInRadius(noEnemiesInRadius, position) == 0;
    }
}
