package atlantis.combat.micro;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.debug.AtlantisPainter;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import atlantis.util.AtlantisUtilities;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APosition;
import atlantis.wrappers.PositionOperationsHelper;
import bwapi.Color;
import bwapi.Position;
import bwta.BWTA;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisRunManager {

    private AUnit unit;
    private APosition runTo;
    private int _updated_at = -1;

    // =========================================================
    public AtlantisRunManager(AUnit unit) {
        this.unit = unit;
    }

    // =========================================================
    private boolean makeUnitRun() {
        if (unit == null || unit.isStuck()) {
            markAsNotRunning();
            return false;
        } else if (runTo == null && !unit.isStartingAttack()) {
            unit.holdPosition();
            return false;
        } else {

            // Update last time run order was issued
            _updated_at = AtlantisGame.getTimeFrames();
//            AtlantisPainter.paintLine(unit.getPosition(), runTo, Color.Yellow);
            unit.move(runTo, UnitActions.RUN_FROM_UNIT);

            // Make all other units very close to it run as well
            notifyNearbyUnitsToMakeSpace(unit);

            return true;
        }
    }

    // =========================================================
    public boolean run() {

        // Define which enemies are considered as close enough to be dangerous
        Units closeEnemies = defineCloseEnemies(unit);
        if (closeEnemies.isEmpty()) {
            markAsNotRunning();
//            System.err.println("No enemies to run to for " + unit);
            return false;
        }

        // Define "center of gravity" for the set of enemies
        APosition median;
        if (closeEnemies.size() <= 2) {
            median = Select.from(closeEnemies.list()).nearestTo(unit).getPosition();
        } else {
            median = closeEnemies.median();
        }

        median = median.makeValidFarFromBounds();

//        // Define closest enemy
//        AUnit nearestEnemy = Select.from(closeEnemies.list()).inRadius(4, unit).nearestTo(unit);
//        
//        // Create weighted "average" enemy position
//        ArrayList<APosition> listToWeigh = new ArrayList<>();
//        listToWeigh.add(median);
//        if (nearestEnemy != null) {
//            listToWeigh.add(median);
////            listToWeigh.add(nearestEnemy.getPosition());
//            listToWeigh.add(nearestEnemy.getPosition());
//        }
//        APosition weightedEnemyPosition = PositionOperationsHelper.getPositionMedian(listToWeigh);
//        AtlantisPainter.paintCircleFilled(weightedEnemyPosition, 4, Color.Orange);
        // Run from given position
        return runFrom(median);
    }

    public boolean runFrom(Object unitOrPosition) {
        if (unitOrPosition == null) {
            System.err.println("Empty position to run to for " + unit);
            markAsNotRunning();
            return false;
        }

        APosition runFrom = null;
        if (unitOrPosition instanceof AUnit) {
            runFrom = ((AUnit) unitOrPosition).getPosition();
        } else if (unitOrPosition instanceof APosition) {
            runFrom = (APosition) unitOrPosition;
        }

//        if (runFrom != null) {
//            System.out.println("Run from " + runFrom + ", dist: " + runFrom.distanceTo(unit));
//        }
        // === Define run to position ==========================
        runTo = getPositionAwayFrom(unit, runFrom);

        if (runTo != null) {
            double dist = runTo.distanceTo(unit);
            unit.setTooltip("" + String.format("%.1f", dist));
        } else {
            unit.setTooltip("NULL");
        }

        // === Actual run ======================================
        return makeUnitRun();
    }

    // =========================================================
    /**
     *
     */
    public static APosition getPositionAwayFrom(AUnit unit, APosition runAwayFrom) {
        if (unit == null || runAwayFrom == null) {
            return null;
        }

//        if (AtlantisGame.getTimeSeconds() <= 350) {
//        return findPositionToRun_preferMainBase(unit, runAwayFrom);
//        }
//        else {
        return findPositionToRun_dontPreferMainBase(unit, runAwayFrom);
//        }
    }

    // =========================================================
    /**
     * Running behavior which will make unit run toward main base.
     */
    private static APosition findPositionToRun_preferMainBase(AUnit unit, APosition runAwayFrom) {
        AUnit mainBase = Select.mainBase();
        if (mainBase != null) {
            if (PositionUtil.distanceTo(mainBase, unit) > 5) {
                return mainBase.getPosition();
//                return mainBase.translated(0, 3 * 64);
            }
        }

        return findPositionToRun_dontPreferMainBase(unit, runAwayFrom);
    }

    /**
     * Running behavior which will make unit run <b>NOT</b> toward main base, but <b>away from the enemy</b>.
     */
    private static APosition findPositionToRun_dontPreferMainBase(AUnit unit, APosition runAwayFrom) {
        APosition runTo = null;

        // === Get standard run to position - as far from enemy as possible
        
        if (!unit.isVulture() || Select.enemyRealUnits().inRadius(2.8, unit).count() <= 1) {
            runTo = findRunPositionShowYourBackToEnemy(unit, runAwayFrom);
        }

        // === Check if the place isn't too close ==================
        // If it is, it probably means we're in the corner and that we should run even towards the enemy,
        // with the hope of getting out.
        
        if (runTo == null) {
            runTo = findRunPositionAtAnyDirection(unit, unit.isVulture() ? 6 : 4);
        }
//        else {
//            AtlantisPainter.paintCircleFilled(unit.getPosition(), 7, Color.Yellow);
//        }

        // === Very rarely it can still be null ====================
        
        if (runTo == null && unit.isVulture()) {
            runTo = findRunPositionShowYourBackToEnemy(unit, runAwayFrom);
        }
        
        if (runTo == null) {
            runTo = findRunPositionAtAnyDirection(unit, 2);
        }

        if (runTo == null) {
            runTo = findRunPositionAtAnyDirection(unit, 10);
        }

        // =========================================================
        if (runTo != null) {
//            AtlantisPainter.paintLine(unit.getPosition(), runTo, Color.Yellow);
            return runTo;
        } else {
//            System.err.println("Couldn't find run position - pretty f'ed up ("
//                    + (runTo != null ? APosition.createFrom(runTo).distanceTo(unit) : "null") + ") ");
            return null;
        }
    }

    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    private static APosition findRunPositionShowYourBackToEnemy(AUnit unit, APosition runAwayFrom) {
        double minTiles = unit.isVulture() ? 1.5 : 2.5;
        double maxDist = minTiles + 2;

//        while (minTiles <= maxTiles) {
        double currentDist = maxDist;
        while (currentDist >= minTiles) {
            
            // Check if this is good position
            APosition runTo = canRunByShowingBackToEnemyTo(unit, runAwayFrom, currentDist, minTiles, maxDist);

            // Also check if can run further (avoid corner shitholes)
            if (runTo != null) {
                double distBonus = unit.isVulture() ? 0.6 : 2.6;
                runTo = canRunByShowingBackToEnemyTo(unit, runAwayFrom, currentDist + distBonus, minTiles, maxDist);
                
                // If is okay as well, return it
                if (runTo != null) {
                    return runTo;
                }
            }
                
            currentDist--;
        }

        return null;
    }

    private static APosition canRunByShowingBackToEnemyTo(AUnit unit, APosition runAwayFrom,
            double dist, double minDist, double maxDist) {
        APosition runTo;
        double vectorLength = unit.distanceTo(runAwayFrom);

        double vectorX = runAwayFrom.getX() - unit.getPosition().getX();
        double vectorY = runAwayFrom.getY() - unit.getPosition().getY();
        double ratio = dist / vectorLength;

        // Apply opposite 2D vector
        runTo = new APosition((int) (unit.getX() - ratio * vectorX), (int) (unit.getY() - ratio * vectorY));

        // === Ensure position is in bounds ========================================
        int oldX = runTo.getX();
        int oldY = runTo.getY();
        runTo = runTo.makeValidFarFromBounds();

        // If vector changed (meaning we almost reached the map boundaries) disallow it
        if (runTo.getX() != oldX || runTo.getY() != oldY) {
            return null;
        }

        // If run distance is acceptably long and it's connected, it's ok.
        if (isPossibleAndReasonablePosition(unit, runTo, dist * 0.8, 1.6 * dist)) {
            return runTo;
        } else {
            return null;
        }
    }

    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and most distant
     * to given runAwayFrom position.
     */
    private static APosition findRunPositionAtAnyDirection(AUnit unit, double expectedLength) {

        // === Define run from ========================================
        
        Units unitsInRadius = Select.enemyRealUnits().melee().inRadius(4, unit).units();
        APosition runAwayFrom = unitsInRadius.median();
        if (runAwayFrom == null) {
            return null;
        }

        // =========================================================
        int tx = unit.getTileX();
        int ty = unit.getTileY();

        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                if (dx == 0 && dy == 0) {
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
                int vectorX = dx;
                int vectorY = dy;
                double length = Math.sqrt(vectorX * vectorX + vectorY * vectorY);

                // Normalize
                vectorX /= length;
                vectorY /= length;

                // Scale vector
                vectorX *= expectedLength;
                vectorY *= expectedLength;

                // Create position
                APosition potentialPosition = APosition.createFrom(
                        (int) (tx + vectorX),
                        (int) (ty + vectorY)
                );

                // Make sure it's inbounds
                if (unitsInRadius.size() <= 1) {
                    potentialPosition = potentialPosition.makeValidFarFromBounds();
                }
                else {
                    potentialPosition = potentialPosition.makeValid();
                }

                // If has path to given point, add it to the list of potential points
                if (isPossibleAndReasonablePosition(unit, potentialPosition, expectedLength * 0.3, 1.6 * expectedLength)) {
                    potentialPositionsList.add(potentialPosition);
//                    AtlantisPainter.paintLine(unit.getPosition(), potentialPosition, Color.Orange);
                }
            }
        }

        // Find the location that would be most distant to the enemy location
        double mostDistant = -1;
        APosition bestPosition = null;
        for (APosition position : potentialPositionsList) {
            double dist = runAwayFrom.distanceTo(position);
            if (bestPosition == null || dist >= mostDistant) {
                bestPosition = position;
                mostDistant = dist;
            }
        }

//        AtlantisPainter.paintCircleFilled(unit.getPosition(), 7, Color.Purple);
//        AtlantisPainter.paintLine(unit.getPosition(), bestPosition, Color.Green);
//        AtlantisPainter.paintLine(unit.getPosition().translateByPixels(1, 1), bestPosition.translateByPixels(1, 1), Color.Green);
        return bestPosition;
    }

    /**
     * Returns true if given run position is traversable, land-connected and not very, very far
     */
    public static boolean isPossibleAndReasonablePosition(AUnit unit, APosition position, double minDist, double maxDist) {
        return position.distanceTo(unit) > (minDist - 0.2) && unit.hasPathTo(position)
                && AtlantisMap.isWalkable(position)
                && AtlantisMap.getGroundDistance(unit, position) <= maxDist;
    }

    private static Units defineCloseEnemies(AUnit unit) {
        double radius;

        if (unit.getType().isVulture()) {
            radius = 5;
        } else {
            radius = 5;
        }

        return Select.enemy().combatUnits().canAttack(unit, radius).units();
    }

    /**
     * Tell other units that might be blocking our escape route to move.
     */
    private void notifyNearbyUnitsToMakeSpace(AUnit unit) {
        double safetyRadiusSize = (unit.getType().getDimensionLeft() + unit.getType().getDimensionUp())
                / 64 * 1.3;

        Select<?> units = Select.ourRealUnits().inRadius(safetyRadiusSize, unit);
        List<AUnit> otherUnits = units.listUnits();
        for (AUnit otherUnit : otherUnits) {
            if (!otherUnit.isRunning() && !unit.equals(otherUnit)) {
                boolean result = otherUnit.runFrom(unit);
                otherUnit.setTooltip("Make space (" + otherUnit.distanceTo(unit) + ")");
            }
        }
    }

    // === Getters ========================================
    public APosition getRunToPosition() {
        return runTo;
    }

    public boolean isRunning() {
        if (runTo != null) {
            int framesAgo = AtlantisGame.getTimeFrames() - _updated_at;
            if (framesAgo <= 1) {
                return true;
            } else {
                markAsNotRunning();
                return false;
            }
        } else {
            return false;
        }
    }

    public void markAsNotRunning() {
        runTo = null;
        _updated_at = -1;
    }

}
