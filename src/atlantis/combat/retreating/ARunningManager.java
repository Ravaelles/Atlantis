package atlantis.combat.retreating;

import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.debug.APainter;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import bwapi.Color;

import java.util.ArrayList;


public class ARunningManager {

    private final AUnit unit;
    private static APosition _lastPosition;
//    private APosition runAwayFrom = null;
    private APosition runTo;
    private Units closeEnemies;
    private APosition enemyMedian = null;

    // =========================================================
    
    public ARunningManager(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================

    public static boolean shouldStopRunning(AUnit unit) {
//        System.out.println(unit.getID() + " // " + unit.isRunning()
//                + " // " + AAvoidUnits.shouldNotAvoidAnyUnit(unit));
        if (
                unit.isRunning()
                && unit.lastStartedRunningMoreThanAgo(5)
                && !unit.lastStoppedRunningLessThanAgo(10)
                && !unit.isUnderAttack(20)
                && AAvoidUnits.shouldNotAvoidAnyUnit(unit)
        ) {
            unit.runningManager().stopRunning();
            unit.setTooltip("StopRun");
            return true;
        }

        return false;
    }

//    public boolean runFromHere() {
//        return runFrom(null, -1);
//    }

    public boolean runFrom(Object unitOrPosition, double dist) {
        if (unitOrPosition == null) {
            System.err.println("Null unit to run from");
            stopRunning();
            throw new RuntimeException("Null unit to run from");
        }

        HasPosition runAwayFrom = (HasPosition) unitOrPosition;
        if (unitOrPosition instanceof AUnit) {
//            runAwayFrom = ((AUnit) unitOrPosition).getPosition();
            AUnit runAwayFromAsUnit = ((AUnit) unitOrPosition);
            runAwayFrom = runAwayFromAsUnit.position();
        } else if (unitOrPosition instanceof APosition) {
            runAwayFrom = (APosition) unitOrPosition;
        }

        // === Define run to position ==============================

//        if (A.notUms() && A.seconds() <= 350 && Count.ourCombatUnits() <= 6 && unit.distToMoreThan(Select.mainBase(), 30)) {
//        if (A.notUms() && A.seconds() <= 350 && (Count.ourCombatUnits() <= 4 && unit.distToMoreThan(Select.mainBase(), 13))) {
//            runTo = Select.mainBase().position();
//        } else {
        runTo = getPositionAwayFrom(unit, runAwayFrom, dist);
//        }

        // === Run to base as a fallback ===========================

        if (runTo == null) {
            runTo = handleRunToMainAsAFallback(unit, runAwayFrom);
        }

        // === Still nothing, try to run anywhere ==================

        if (runTo == null) {
            runTo = findPositionToRunInAnyDirection(unit, runAwayFrom);
        }

        // === Actual run order ====================================

        if (runTo != null && runTo.distTo(unit) >= 0.02) {
            dist = runTo.distTo(unit);
            unit.setTooltip("StartRun(" + String.format("%.1f", dist) + ")");
            return makeUnitRun();
        }

        System.err.println("====================");
        System.err.println(unit.position());
        System.err.println(runTo);
        System.err.println(_lastPosition);
        System.err.println(Select.ourIncludingUnfinished().exclude(unit).inRadius(unit.size(), unit).count());
        System.err.println(Select.neutral().inRadius(unit.size(), unit).count());
        System.err.println();
//                && unit.position().groundDistanceTo(position) <= 18


        unit.setTooltip("Cant run");
        return false;
    }

    private APosition handleRunToMainAsAFallback(AUnit unit, HasPosition runAwayFrom) {
        if (Select.main() != null && Select.main().distToMoreThan(runAwayFrom, 15)) {
            if (Select.our().inRadius(0.7, unit).exclude(unit).atMost(2)) {
                return Select.main().position();
            }
        }

        return null;
    }

//    public boolean runFromCloseEnemies() {
//
//        // Define which enemies are considered as close enough to be dangerous
//        closeEnemies = defineCloseEnemies(unit);
//        if (closeEnemies.isEmpty()) {
//            stopRunning();
//            return false;
//        }
//
//        // ===========================================
//        // Define "center of gravity" for the set of enemies
//
//        enemyMedian = closeEnemies.averageDistanceWeightedTo(unit, 0.33);
//        APainter.paintCircleFilled(enemyMedian, 10, Color.Orange);
//
//        // Run from given position
//        return runFrom(enemyMedian, -1);
//    }

    // =========================================================
    
    /**
     * Running behavior which will make unit run <b>NOT</b> toward main base, but <b>away from the enemy</b>.
     */
    private APosition findBestPositionToRun(AUnit unit, HasPosition runAwayFrom, double dist) {
        APosition runTo = null;

        // === Run directly away from the enemy ========================================
        
        if (!unit.position().isCloseToMapBounds() && (closeEnemies == null || closeEnemies.size() <= 1)) {
            if (runAwayFrom == null && closeEnemies != null && closeEnemies.size() == 1) {
                runAwayFrom = closeEnemies.first().position();
            }
            runTo = findRunPositionShowYourBackToEnemy(unit, runAwayFrom, dist);
        }
        
        // === Get run to position - as far from enemy as possible =====================

        if (runTo == null) {
            runTo = findPositionToRunInAnyDirection(unit, runAwayFrom);
        }
        
        // =============================================================================

        if (
                runTo != null && runTo.distTo(unit) <= 0.3
                && isPossibleAndReasonablePosition(unit, runTo.position(), true)
        ) {
            System.err.println("Invalid run position, dist = " + runTo.distTo(unit));
            APainter.paintLine(unit, runTo, Color.Purple);
            APainter.paintLine(
                    unit.position().translateByPixels(0, 1),
                    runTo.translateByPixels(0, 1),
                    Color.Purple
            );
            APainter.paintCircleFilled(runTo, 8, Color.Red);
        }

        // =============================================================================

        return runTo;
    }
    
    // =========================================================
    
    /**
     *
     */
    public APosition getPositionAwayFrom(AUnit unit, HasPosition runAwayFrom, double dist) {
        if (unit == null || runAwayFrom == null) {
            return null;
        }

//        if (AGame.getTimeSeconds() <= 250 && shouldRunTowardsMainBase(unit, runAwayFrom)) {
//            return Select.mainBase().getPosition();
//        }

        return unit.runningManager().findBestPositionToRun(unit, runAwayFrom, dist);
    }

    // =========================================================
    /**
     * Running behavior which will make unit run toward main base.
     */
//    private boolean shouldRunTowardsMainBase(AUnit unit, APosition runAwayFrom) {
//        AUnit mainBase = Select.mainBase();
//        if (mainBase != null) {
//            if (PositionUtil.distanceTo(mainBase, unit) > 30) {
//                return true;
////                return mainBase.translated(0, 3 * 64);
//            }
//        }
//
//        return false;
////        return findPositionToRun_preferAwayFromEnemy(unit, runAwayFrom);
//    }

    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    private APosition findRunPositionShowYourBackToEnemy(AUnit unit, HasPosition runAwayFrom, double dist) {
        double minTiles = dist >= 1 ? dist : 1.1;
        double maxDist = dist >= 1 ? dist : 3.0;

        double currentDist = maxDist;
        while (currentDist >= minTiles) {

            // Check if this is good position
            APosition runTo = canRunByShowingBackToEnemyTo(unit, runAwayFrom, currentDist);

            // Also check if can run further (avoid corner shitholes)
            if (runTo != null) {
//                double distBonus = unit.isVulture() ? 2 : 1;
                APosition doubleRunTo = canRunByShowingBackToEnemyTo(
                        unit, runAwayFrom, currentDist
                );

                // If is okay as well, return it
                if (doubleRunTo != null) {
                    return runTo;
                }
            }

            currentDist -= 0.9;
        }

        return null;
    }

    private APosition canRunByShowingBackToEnemyTo(AUnit unit, HasPosition runAwayFrom, double dist) {
        APosition runTo;
        double vectorLength = unit.position().distTo(runAwayFrom);

        double vectorX = runAwayFrom.x() - unit.position().getX();
        double vectorY = runAwayFrom.y() - unit.position().getY();
        double ratio = dist / vectorLength;

        // Apply opposite 2D vector
        runTo = new APosition((int) (unit.x() - ratio * vectorX), (int) (unit.y() - ratio * vectorY));

        // === Ensure position is in bounds ========================================
        
        int oldX = runTo.getX();
        int oldY = runTo.getY();

//        runTo = runTo.makeValidFarFromBounds();
        runTo = runTo.makeValidFarFromBounds();

        // If vector changed (meaning we almost reached the map boundaries) disallow it
        if (runTo.getX() != oldX || runTo.getY() != oldY) {
            return null;
        }
        
        // =========================================================

        // If run distance is acceptably long and it's connected, it's ok.
        if (isPossibleAndReasonablePosition(unit, runTo, true, "O", "X")) {
            APainter.paintLine(unit.position(), runTo, Color.Purple);
            APainter.paintLine(unit.position().translateByPixels(-1, -1), runTo, Color.Purple);
//            APainter.paintLine(unit.getPosition().translateByPixels(1, 1), runTo, Color.Purple);
            return runTo;
        } else {
            return null;
        }
    }

    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and most distant
     * to given runAwayFrom position.
     */
    private APosition findPositionToRunInAnyDirection(AUnit unit, HasPosition runAwayFrom) {

        // === Define run from ====================================================
//        Units unitsInRadius = Select.enemyRealUnits().melee().inRadius(4, unit).units();
//        APosition runAwayFrom = unitsInRadius.median();
        if (runAwayFrom == null) {
            System.err.println("Run away from is null in findRunPositionAtAnyDirection");
            return null;
        }
        
        // === Define if we don't want to go towards region polygon points ========

//        boolean avoidCornerPoints = AMap.getDistanceToAnyRegionPolygonPoint(unit.getPosition()) > 1.5;
        
        // ========================================================================
        
        APosition unitPosition = unit.position();
        int radius = runDistanceForAnyDirection(unit);
        APosition bestPosition = null;
        while (bestPosition == null && radius >= 0.5) {
            bestPosition = findRunPositionInRadius(unitPosition, runAwayFrom, radius);
            radius -= 1;
        }
        
        // =========================================================

//        if (bestPosition != null) {
//            APainter.paintLine(unit, bestPosition, Color.Green);
//            APainter.paintLine(unit.getPosition().translateByPixels(1, 1), bestPosition.translateByPixels(1, 1), Color.Green);
//        }
        
//        AtlantisPainter.paintCircleFilled(unit.getPosition(), 7, Color.Purple);
//        AtlantisPainter.paintLine(unit.getPosition(), bestPosition, Color.Green);
//        AtlantisPainter.paintLine(unit.getPosition().translateByPixels(1, 1), bestPosition.translateByPixels(1, 1), Color.Green);
        return bestPosition;
    }

    private APosition findRunPositionInRadius(APosition unitPosition, HasPosition runAwayFrom, int radius) {

        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<>();
//        APainter.paintCircleFilled(enemyMedian, 8, Color.Purple); // @PAINT EnemyMedian

        for (int dtx = -radius; dtx <= radius; dtx++) {
            for (int dty = -radius; dty <= radius; dty++) {
                if (dtx != -radius && dtx != radius && dty != -radius && dty != radius) {
                    continue;
                }

                // Create position, Make sure it's inbounds
                APosition potentialPosition = unitPosition.translateByTiles(dtx, dty).makeValidFarFromBounds();

                // If has path to given point, add it to the list of potential points
                APainter.paintLine(unitPosition, potentialPosition, Color.Purple);
                if (isPossibleAndReasonablePosition(unit, potentialPosition, true, "v", "x")) {
                    potentialPositionsList.add(potentialPosition);
                }
            }
        }

//        System.out.println("potentialPositionsList = " + potentialPositionsList.size());

        // =========================================================
        // Find the location that would be most distant to the enemy location
        double mostDistant = -99;
        APosition bestPosition = null;
        for (APosition position : potentialPositionsList) {
            double dist = position.distTo(runAwayFrom);
            if (bestPosition == null || dist >= mostDistant) {
                bestPosition = position;
                mostDistant = dist;
            }
        }

        return bestPosition;
    }

    private int runDistanceForAnyDirection(AUnit unit) {
        if (unit.isVulture()){
            return 5;
        }

        if (unit.isInfantry()) {
            return 4;
        }

        return 4;
    }

    /**
     * Tell other units that might be blocking our escape route to move.
     */
    private boolean notifyNearbyUnitsToMakeSpace(AUnit unit) {
        if (unit.isAirUnit() || unit.isLoaded()) {
            return false;
        }

        Selection friendsTooClose = Select.ourRealUnits()
                .exclude(unit).groundUnits().inRadius(0.17 + unit.woundPercent() / 300.0, unit);

        if (friendsTooClose.count() <= 1) {
            return false;
        }

        for (AUnit otherUnit : friendsTooClose.list()) {
            if (canBeNotifiedToMakeSpace(otherUnit)) {
                otherUnit.runningManager().runFrom(unit, 0.6);
                APainter.paintCircleFilled(unit, 10, Color.Yellow);
                APainter.paintCircleFilled(otherUnit, 7, Color.Grey);
                otherUnit.setTooltip("MakeSpace" + A.dist(otherUnit, unit));
            }
        }
        return true;
    }

    private boolean canBeNotifiedToMakeSpace(AUnit unit) {
        return !unit.isRunning() && !unit.type().isReaver() && unit.lastStartedRunningMoreThanAgo(3);
//        return !unit.isRunning() && !unit.type().isReaver();
    }

    // =========================================================
    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public boolean isPossibleAndReasonablePosition(
            AUnit unit, APosition position, boolean includeUnitCheck
    ) {
        return isPossibleAndReasonablePosition(unit, position, includeUnitCheck, "#", "%");
    }

    public boolean isPossibleAndReasonablePosition(
            AUnit unit, APosition position, boolean includeUnitCheck, String charForIsOk, String charForNotOk
    ) {
        if (unit.isAirUnit()) {
            return true;
        }

        _lastPosition = position;

        boolean isOkay = position.isWalkable()
                && (
                    (
                            position.translateByPixels(-48, -48).isWalkable()
                            && position.translateByPixels(48, 48).isWalkable()
                            && position.translateByPixels(48, -48).isWalkable()
                            && position.translateByPixels(-48, -48).isWalkable()
                    )
//                    || (
//                            position.translateByTiles(0, 1).isWalkable()
//                            && position.translateByTiles(-1, 0).isWalkable()
//                    )
                )
//                && (!includeUnitCheck || Select.our().exclude(this.unit).inRadius(0.6, position).count() <= 0)
                && Select.ourIncludingUnfinished().exclude(unit).inRadius(unit.size(), position).count() <= 0
                && Select.neutral().inRadius(unit.size(), position).isEmpty()
                && unit.hasPathTo(position)
                && unit.position().groundDistanceTo(position) <= 18
//                && Select.neutral().inRadius(1.2, position).count() == 0
//                && Select.enemy().inRadius(1.2, position).count() == 0
//                && Select.ourBuildings().inRadius(1.2, position).count() == 0
                ;

        if (charForIsOk != null) {
            APainter.paintTextCentered(position, isOkay ? charForIsOk : charForNotOk, isOkay ? Color.Green : Color.Red);
        }

//        System.err.println(unit + " @" + (int) AtlantisMap.getGroundDistance(unit, position));

//        if (isOkay && !allowCornerPointsEtc) {
////        if (isOkay && !allowCornerPointsEtc && AMap.getDistanceToAnyRegionPolygonPoint(unitPosition) < 1) {
////            isOkay = AMap.getDistanceToAnyRegionPolygonPoint(unitPosition);
//            isOkay = false;
//        }

        return isOkay;
    }


    private boolean makeUnitRun() {
        if (unit == null) {
            return false;
        }
        if (runTo == null) {
            stopRunning();
            unit.setTooltip("Fuck!");
            return true;
        }

        // === Valid run position ==============================

        else {
//            System.err.println("Run manager, run dist: " + runTo.distanceTo(unit));

            // Update last time run order was issued
            unit._lastStartedRunning = A.now();
            unit.move(runTo, UnitActions.RUN, "Run(" + A.digit(unit.distTo(runTo)) + ")");

            // Make all other units very close to it run as well
            notifyNearbyUnitsToMakeSpace(unit);

//            if (hasMoved) {
            return true;
//            } else {
//                APosition position = unit.getPosition();
////                APainter.paintLine(position.translateByPixels(-26, -26), position.translateByPixels(25, 25), Color.Red);
////                APainter.paintLine(position.translateByPixels(-25, -25), position.translateByPixels(26, 26), Color.Red);
////                APainter.paintLine(position.translateByPixels(-26, 26), position.translateByPixels(25, -25), Color.Red);
////                APainter.paintLine(position.translateByPixels(-25, 25), position.translateByPixels(26, -26), Color.Red);
//                stopRunning();
//                return false;
//            }
        }
    }

    // === Getters ========================================

    public APosition getRunToPosition() {
        return runTo;
    }

    public boolean isRunning() {
        if (runTo != null && unit.distTo(runTo) >= 0.3) {
            return true;
//            if (unit.lastStartedRunningAgo(3)) {
//                return true;
//            } else {
//                stopRunning();
//                return false;
//            }
        }

        stopRunning();
        return false;
    }

    public void stopRunning() {
        runTo = null;
        unit._lastStoppedRunning = A.now();
    }
}
