package atlantis.combat.micro;

import atlantis.debug.APainter;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import bwapi.Color;

import java.util.ArrayList;


public class ARunManager {

    private static final int RUN_ANY_DIRECTION_GRID_BORDER = 5;
    
    // =========================================================
    
    private AUnit unit;
    private APosition runAwayFrom = null;
    private APosition runTo;
    private int _updated_at = -1;
    private Units closeEnemies;
    private APosition enemyMedian = null;

    // =========================================================
    
    public ARunManager(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================
    
    private boolean makeUnitRun() {
        if (unit == null) {
            return false;
        } 
        
        // === Unit STUCK ======================================
        
//        else if (unit.isStuck()) {
//            unit.setTooltip("Stuck!!!");
////            unit.holdPosition();
//            stopRunning();
//            return false;
//        }
        
        // === Run to is EMPTY =================================
        
        else if (runTo == null) {
//            boolean finallyRunned = false;
//            for (int dist = 5; dist >= 2.2; dist -= 2) {
//                if (finallyRunned = unit.moveAwayFrom(enemyMedian, dist, "Run")) {
//                    break;
//                }
//            }
//
//            if (!finallyRunned) {
//                stopRunning();
//                unit.setTooltip("Fuck!");
//                return true;
//            }
//            else {
//                return true;
//            }
            stopRunning();
            unit.setTooltip("Fuck!");
            return true;
        }
        
        // === Valid run position ==============================
        
        else {
//            System.err.println("Run manager, run dist: " + runTo.distanceTo(unit));

            // Update last time run order was issued
            _updated_at = A.now();
            unit._lastStartedRunning = A.now();
//            APainter.paintLine(unit.getPosition(), runTo, Color.Orange);
//            boolean hasMoved = unit.move(runTo, UnitActions.RUN);
            unit.move(runTo, UnitActions.RUN, "Run");

            // Make all other units very close to it run as well
//            notifyNearbyUnitsToMakeSpace(unit);

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

    public static boolean shouldStopRunning(AUnit unit) {
        if (unit.isRunning()) {
            AUnit nearEnemy = Select.enemyRealUnits().combatUnits().canAttack(unit, 3).nearestTo(unit);
            if (nearEnemy == null || !nearEnemy.isOtherUnitFacingThisUnit(unit)) {
                unit.getRunManager().stopRunning();
                return true;
            }
        }

        return false;
    }

    // =========================================================
    
    /**
     * Running behavior which will make unit run <b>NOT</b> toward main base, but <b>away from the enemy</b>.
     */
    private APosition findBestPositionToRun(AUnit unit, APosition runAwayFrom) {
        APosition runTo = null;

        // === Run directly away from the enemy ========================================
        
        if (!unit.getPosition().isCloseToMapBounds() && (closeEnemies == null || closeEnemies.size() <= 1)) {
            if (runAwayFrom == null && closeEnemies != null && closeEnemies.size() == 1) {
                runAwayFrom = closeEnemies.first().getPosition();
            }
            runTo = findRunPositionShowYourBackToEnemy(unit, runAwayFrom);
        }
        
        // === Get run to position - as far from enemy as possible =====================

        if (runTo == null) {
            runTo = findRunPositionAtAnyDirection(unit, runAwayFrom);
        }
        
        // =============================================================================

        if (runTo != null && runTo.distanceTo(unit) < 1) {
            System.err.println("Invalid run position, dist = " + runTo.distanceTo(unit));
            APainter.paintLine(unit, runTo, Color.Red);
            APainter.paintLine(
                    unit.getPosition().translateByPixels(0, 1),
                    runTo.translateByPixels(0, 1),
                    Color.Red
            );
            APainter.paintCircleFilled(runTo, 8, Color.Red);
        }

        // =============================================================================

        return runTo;
    }
    
    // =========================================================
    
    public boolean run() {

        // Define which enemies are considered as close enough to be dangerous
        closeEnemies = defineCloseEnemies(unit);
//        APainter.paintTextCentered(unit, "" + closeEnemies.size(), Color.White);
        if (closeEnemies.isEmpty()) {
            stopRunning();
//            APainter.paintTextCentered(unit.getPosition().translateByPixels(0, -15), "No enemies!", Color.Red);
//            System.err.println("No enemies to run to for " + unit);
            return false;
        }
//        else {
//            for (AUnit enemy : closeEnemies.list()) {
//                APainter.paintCircle(enemy.getPosition(), 10, Color.Green);
//                APainter.paintCircle(enemy.getPosition(), 9, Color.Green);
//                APainter.paintCircle(enemy.getPosition(), 6, Color.Green);
//                APainter.paintCircle(enemy.getPosition(), 5, Color.Green);
//            }
//        }

        // ===========================================
        // Define "center of gravity" for the set of enemies
        
//        if (closeEnemies.size() <= maxEnemiesToRunFromNearestEnemy) {
//            enemyMedian = Select.from(closeEnemies.list()).nearestTo(unit).getPosition();
//        } else {
//            enemyMedian = closeEnemies.median();
//            enemyMedian = closeEnemies.average();
        enemyMedian = closeEnemies.averageDistanceWeightedTo(unit, 0.33);
        APainter.paintLine(unit, enemyMedian, Color.Yellow);
//        }
//        enemyMedian = enemyMedian.makeValid();
        
//        APainter.paintCircleFilled(enemyMedian, 12, Color.Red);
        
        // Run from given position
        return runFrom(enemyMedian);
    }

    public boolean runFrom(Object unitOrPosition) {
        if (unitOrPosition == null) {
            System.err.println("Null unit to run from");
            stopRunning();
            throw new RuntimeException("Null unit to run from");
        }

        runAwayFrom = null;
        if (unitOrPosition instanceof AUnit) {
            runAwayFrom = ((AUnit) unitOrPosition).getPosition();
        } else if (unitOrPosition instanceof APosition) {
            runAwayFrom = (APosition) unitOrPosition;
        }
        
        // === Define run to position ==============================
        
        runTo = getPositionAwayFrom(unit, runAwayFrom);
        
        // =========================================================

        if (runTo != null) {
            double dist = runTo.distanceTo(unit);
            unit.setTooltip("StartRun(" + String.format("%.1f", dist) + ")");
        } else {
            unit.setTooltip("Cant run");
        }

        // === Actual run order ====================================
        
        return makeUnitRun();
    }
    
    // =========================================================
    
    /**
     *
     */
    public APosition getPositionAwayFrom(AUnit unit, APosition runAwayFrom) {
        if (unit == null || runAwayFrom == null) {
            return null;
        }

//        if (AGame.getTimeSeconds() <= 250 && shouldRunTowardsMainBase(unit, runAwayFrom)) {
//            return Select.mainBase().getPosition();
//        }

        return unit.getRunManager().findBestPositionToRun(unit, runAwayFrom);
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
    private APosition findRunPositionShowYourBackToEnemy(AUnit unit, APosition runAwayFrom) {
//        double minTiles = unit.isVulture() ? 5.5 : (unit.isWorker() ? 3 : 1.2);
        double minTiles = 1.1;
        double maxDist = 3.0;

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

    private APosition canRunByShowingBackToEnemyTo(AUnit unit, APosition runAwayFrom, double dist) {
        APosition runTo;
        double vectorLength = unit.getPosition().distanceTo(runAwayFrom);

        double vectorX = runAwayFrom.getX() - unit.getPosition().getX();
        double vectorY = runAwayFrom.getY() - unit.getPosition().getY();
        double ratio = dist / vectorLength;

        // Apply opposite 2D vector
        runTo = new APosition((int) (unit.getX() - ratio * vectorX), (int) (unit.getY() - ratio * vectorY));

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
        if (isPossibleAndReasonablePosition(unit.getPosition(), runTo, "O", "X")) {
            APainter.paintLine(unit.getPosition(), runTo, Color.Purple);
            APainter.paintLine(unit.getPosition().translateByPixels(-1, -1), runTo, Color.Purple);
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
    private APosition findRunPositionAtAnyDirection(AUnit unit, APosition runAwayFrom) {

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
        
        APosition unitPosition = unit.getPosition();
//        int tx = unitPosition.getTileX();
//        int ty = unitPosition.getTileY();

        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<>();
//        APainter.paintCircleFilled(enemyMedian, 8, Color.Purple); // @PAINT EnemyMedian

        int border = unit.isVulture() ? 3 : RUN_ANY_DIRECTION_GRID_BORDER;
        for (int dtx = -border; dtx <= border; dtx++) {
            for (int dty = -border; dty <= border; dty++) {
                if (dtx != -border && dtx != border && dty != -border && dty != border) {
                    continue;
                }
                
//                // Define point
//                APosition potentialPosition = APosition.createFrom(
//                        tx + dx * expectedVectorLength, ty + dy * expectedVectorLength
//                );
//                
//                // Scale vector if needed
//                double vectorLength = potentialPosition.distanceTo(unit) + 0.01;
//                if (Math.abs(vectorLength - expectedVectorLength) > 0.1) {
//                    potentialPosition = APosition.createFrom(
//                            (int) (tx + dx * vectorLength * expectedVectorLength / vectorLength), 
//                            (int) (ty + dy * expectedVectorLength / vectorLength)
//                    );
//                }
                // Define vevtor
//                double vectorX = dtx;
//                double vectorY = dty;
//                double vectorLength = Math.sqrt(vectorX * vectorX + vectorY * vectorY);
//
//                // Normalize
//                vectorX /= vectorLength;
//                vectorY /= vectorLength;
//
//                // Scale vector
//                vectorX *= expectedLength;
//                vectorY *= expectedLength;
//                vectorLength = Math.sqrt(vectorX * vectorX + vectorY * vectorY);

                // Create position, Make sure it's inbounds
                APosition potentialPosition = unitPosition.translateByTiles(dtx, dty).makeValidFarFromBounds();

                // If has path to given point, add it to the list of potential points
//                APainter.paintLine(unitPosition, potentialPosition, Color.Red);
                if (isPossibleAndReasonablePosition(unitPosition, potentialPosition, "v", "x")) {
                    potentialPositionsList.add(potentialPosition);
                    
                    // Check if position slightly further is walkable as well
//                    APosition furtherPosition = APosition.create(
//                            (int) (tx + vectorX * (expectedLength + 1.5) / expectedLength),
//                            (int) (ty + vectorY * (expectedLength + 1.5) / expectedLength)
//                    );
//                    APainter.paintLine(unitPosition, furtherPosition, Color.Orange);
                    
//                    if (isPossibleAndReasonablePosition(
//                            unitPosition, furtherPosition,expectedLength * 0.6, 3 * expectedLength, false
//                    )) {
////                        expectedLength * 0.6, 3 * expectedLength, avoidCornerPoints)) {
//                        potentialPositionsList.add(potentialPosition);
////                        APainter.paintLine(unitPosition, potentialPosition, Color.Teal);
//                    }
                }
            }
        }

//        System.out.println("potentialPositionsList = " + potentialPositionsList.size());
        
        // =========================================================
        // Find the location that would be most distant to the enemy location
        double mostDistant = -99999;
        APosition bestPosition = null;
        for (APosition position : potentialPositionsList) {
            double dist = runAwayFrom.distanceTo(position);
            if (bestPosition == null || dist >= mostDistant) {
                bestPosition = position;
                mostDistant = dist;
            }
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

    private static Units defineCloseEnemies(AUnit unit) {
//        return Select.enemy().combatUnits().canAttack(unit, 6).units();

        Select<AUnit> veryCloseEnemies = Select.enemy().combatUnits().canAttack(unit, 4);
        if (veryCloseEnemies.size() > 0 && veryCloseEnemies.size() <= 1) {
            return veryCloseEnemies.units();
        }
        else {
            return Select.enemy().combatUnits().canAttack(unit, 4).units();
        }
    }

    /**
     * Tell other units that might be blocking our escape route to move.
     */
//    private void notifyNearbyUnitsToMakeSpace(AUnit unit) {
//        double safetyRadiusSize = (unit.getType().getDimensionLeft() + unit.getType().getDimensionUp())
//                / 64 * 1.35;
//
//        Select<?> units = Select.ourRealUnits().inRadius(safetyRadiusSize, unit);
//        List<AUnit> otherUnits = units.listUnits();
//        for (AUnit otherUnit : otherUnits) {
//            if (!otherUnit.isRunning() && !unit.equals(otherUnit)) {
//                boolean result = otherUnit.runFrom(unit);
//                otherUnit.setTooltip("Make space (" + otherUnit.distanceTo(unit) + ")");
//            }
//        }
//    }

    // =========================================================
    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public static boolean isPossibleAndReasonablePosition(
            APosition unitPosition, APosition position
    ) {
        return isPossibleAndReasonablePosition(unitPosition, position, "#", "*");
    }

    public static boolean isPossibleAndReasonablePosition(
            APosition unitPosition, APosition position, String charForIsOk, String charForNotOk
    ) {

//        boolean isOkay = position.distanceTo(unit) > (minDist - 0.2) 
//                && AMap.isWalkable(position) && unit.hasPathTo(position)
//                && Select.all().inRadius(0.2, position).count() <= 1
//                //                && Atlantis.getBwapi().getUnitsInRadius(unit, 1).isEmpty()
//                //                && AtlantisMap.isWalkable(position.translateByTiles(-1, -1))
//                //                && AtlantisMap.isWalkable(position.translateByTiles(1, 1))
//                && AMap.getGroundDistance(unit, position) <= maxDist;
//                ;
//
//        if (!AMap.isWalkable(position)) {
//            APainter.paintCircleFilled(position, 10, Color.Red);
//        }
//        
//        if (!AMap.isWalkable(position.translateTilesTowards(unitPosition, -1))) {
//            APainter.paintCircleFilled(position, 16, Color.Yellow);
//        }

//        boolean isOkay = AMap.isWalkable(position)
        boolean isOkay = AMap.isWalkable(position)
                && (
                    AMap.isWalkable(position.translateByTiles(1, 0))
                    && AMap.isWalkable(position.translateByTiles(-1, 0))
                    && AMap.isWalkable(position.translateByTiles(0, 1))
                    && AMap.isWalkable(position.translateByTiles(0, -1))
                )
                && Select.all().inRadius(2.0, position).isEmpty()
                && Select.neutral().inRadius(3.5, position).isEmpty()
                && unitPosition.hasPathTo(position)
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

    // === Getters ========================================

    public APosition getRunToPosition() {
        return runTo;
    }

    public boolean isRunning() {
        if (runTo != null) {
            if (unit.lastStartedRunningAgo(3)) {
                return true;
            } else {
                stopRunning();
                return false;
            }
        }

        return false;
    }

    public void stopRunning() {
        runTo = null;
        _updated_at = -1;
    }

}
