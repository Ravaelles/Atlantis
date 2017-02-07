package atlantis.combat.micro;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.debug.AtlantisPainter;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.missions.UnitActions;
import atlantis.util.AtlantisUtilities;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APosition;
import atlantis.wrappers.PositionOperationsHelper;
import bwapi.Color;
import bwapi.Position;
import java.util.ArrayList;

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
        if (unit == null || runTo == null || unit.isStuck()) {
            runTo = null;
            return false;
        }
        else {
            _updated_at = AtlantisGame.getTimeFrames();
//            AtlantisPainter.paintLine(unit.getPosition(), runTo, Color.Yellow);
            unit.move(runTo, UnitActions.RUN_FROM_UNIT);
            return true;
        }
    }
    
    // =========================================================
    
    public boolean run() {
        
        // Define which enemies are considered as close enough to be dangerous
        Units closeEnemies = defineCloseEnemies(unit);
        if (closeEnemies.isEmpty()) {
            runTo = null;
//            System.err.println("No enemies to run to for " + unit);
            return false;
        }
        
        // Define "center of gravity" for the set of enemies
        APosition median;
        if (closeEnemies.size() <= 2) {
            median = Select.from(closeEnemies.list()).nearestTo(unit).getPosition();
        }
        else {
            median = closeEnemies.median();
        }
        
        median = median.makeValidFarFromBounds();
        
//        AtlantisPainter.paintCircle(median, 1, Color.Red);
//        AtlantisPainter.paintCircle(median, 3, Color.Red);
//        AtlantisPainter.paintCircle(median, 5, Color.Red);
//        AtlantisPainter.paintCircle(median, 7, Color.Red);
        
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
            runTo = null;
            return false;
        }
        
        APosition runFrom = null;
        if (unitOrPosition instanceof AUnit) {
            runFrom = ((AUnit) unitOrPosition).getPosition();
        }
        else if (unitOrPosition instanceof APosition) {
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
        }
        else {
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
        
        runTo = findRunPositionShowYourBackToEnemy(unit, runAwayFrom);
        
        // === Check if the place isn't too close ==================
        // If it is, it probably means we're in the corner and that we should run even towards the enemy,
        // with the hope of getting out.
        
        if (runTo == null) {
            runTo = findRunPositionAtAnyDirection(unit);
        }
//        else {
//            AtlantisPainter.paintCircleFilled(unit.getPosition(), 7, Color.Yellow);
//        }

        // =========================================================
        
        if (runTo != null) {
            AtlantisPainter.paintLine(unit.getPosition(), runTo, Color.Yellow);
            return runTo;
        }
        else {
            System.err.println("Couldn't find run position - pretty f'ed up (" 
                    + (runTo != null ? APosition.createFrom(runTo).distanceTo(unit) : "null") + ") ");
            return null;
        }
    }
    
    /**
     * Simplest case: add enemy-to-you-vector to your own position.
     */
    private static APosition findRunPositionShowYourBackToEnemy(AUnit unit, APosition runAwayFrom) {
        double minTiles = unit.isVulture() ? 2.7 : 1;
        double maxTiles = minTiles + 4;
        
        APosition runTo = null;
        
        double vectorX = runAwayFrom.getX() - unit.getPosition().getX();
        double vectorY = runAwayFrom.getY() - unit.getPosition().getY();
        
        while (minTiles <= maxTiles) {
            double vectorLength = unit.distanceTo(runAwayFrom);
            double ratio = minTiles / vectorLength;

            // Apply opposite 2D vector
            runTo = new APosition((int) (unit.getX() - ratio * vectorX), (int) (unit.getY() - ratio * vectorY));
            
//            double angle = 40.5;
//            double rotatedX = (vectorX * Math.cos(angle)) - (vectorY * Math.sin(angle));
//            double rotatedY = (vectorX * Math.sin(angle)) + (vectorY * Math.cos(angle));
//            vectorX = rotatedX;
//            vectorY = rotatedY;
//            runTo = new APosition(
//                    (int) (unit.getPosition().getX() - ratio * vectorX),
//                    (int) (unit.getPosition().getY() - ratio * vectorY)
//            );
            
            // === Ensure position is in bounds ========================================
            
            int oldX = runTo.getX();
            int oldY = runTo.getY();
            runTo = runTo.makeValidFarFromBounds();
            
            if (runTo.getX() != oldX || runTo.getY() != oldY) {
                runTo = null;
                break;
            }

//            if (Atlantis.getBwapi().isBuildable(runTo.toTilePosition(), true) 
//                    && unit.hasPathTo(runTo.getPoint())
//                    && Atlantis.getBwapi().hasPath(unit.getPosition(), runTo)
//                    && BWTA.isConnected(unit.getPosition().toTilePosition(), runTo.toTilePosition())) {

//            System.out.println("Atlantis.getBwapi().isBuildable(runTo.toTilePosition(), true) = " + Atlantis.getBwapi().isBuildable(runTo.toTilePosition(), true));
//            System.out.println("unit.hasPathTo(runTo.getPoint()) = " + unit.hasPathTo(runTo.getPoint()));
            if (runTo.distanceTo(unit) > (minTiles - 0.2) && unit.hasPathTo(runTo) 
                    && AtlantisMap.isWalkable(runTo)) {
                break;
            } else {
                minTiles++;
                
                if (minTiles > maxTiles) {
                    runTo = null;
                    break;
                }
            }
        }
        
        return runTo;
    }
    
    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and
     * most distant to given runAwayFrom position.
     */
    private static APosition findRunPositionAtAnyDirection(AUnit unit) {
        
        // === Define run from ========================================
        
        APosition runAwayFrom = Select.enemyRealUnits().melee().inRadius(4, unit).units().median();
        if (runAwayFrom == null) {
            return null;
        }
        
        // =========================================================
        
        int expectedLength = unit.isVulture() ? 5 : 3;
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
                
//                System.out.println();
//                System.out.println(Math.sqrt(vectorX * vectorX + vectorY * vectorY));
//                System.out.println("RUNTO length: " + potentialPosition.distanceTo(unit));
                
                // Make sure it's inbounds
                potentialPosition = potentialPosition.makeValidFarFromBounds();
                
                // If has path to given point, add it to the list of potential points
                if (unit.hasPathTo(potentialPosition) && AtlantisMap.isWalkable(potentialPosition)) {
                    potentialPositionsList.add(potentialPosition);
                    AtlantisPainter.paintLine(unit.getPosition(), potentialPosition, Color.Orange);
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
        
        AtlantisPainter.paintLine(unit.getPosition(), bestPosition, Color.Green);
        AtlantisPainter.paintLine(unit.getPosition().translateByPixels(1, 1), bestPosition.translateByPixels(1, 1), Color.Green);
        return bestPosition;
    }
    
    private static Units defineCloseEnemies(AUnit unit) {
        double radius;
        
        if (unit.getType().isVulture()) {
            radius = 5;
        }
        else {
            radius = 5;
        }
        
        return Select.enemy().combatUnits().canAttack(unit, radius).units();
    }
    
    // === Getters ========================================
    
    public APosition getRunToPosition() {
        return runTo;
    }
    
    public boolean isRunning() {
        if (runTo != null) {
            int framesAgo = AtlantisGame.getTimeFrames() - _updated_at;
            if (framesAgo <= 2) {
                _updated_at = AtlantisGame.getTimeFrames();
                return true;
            }
            else {
                runTo = null;
                return false;
            }
        }
        else {
            return false;
        }
    }
    
}
