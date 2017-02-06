package atlantis.combat.micro;

import atlantis.debug.AtlantisPainter;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.missions.UnitMissions;
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
    
    // =========================================================

    public AtlantisRunManager(AUnit unit) {
        this.unit = unit;
    }
    
    // =========================================================

    private boolean makeUnitRun() {
        if (unit == null || runTo == null) {
            return false;
        }
        else {
            unit.move(runTo, UnitMissions.RUN_FROM_UNIT);
            return true;
        }
    }
    
    // =========================================================
    
    public boolean run() {
        
        // Define which enemies are considered as close enough to be dangerous
        Units closeEnemies = defineCloseEnemies(unit);
        if (closeEnemies.isEmpty()) {
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
        
        AtlantisPainter.paintCircle(median, 1, Color.Red);
        AtlantisPainter.paintCircle(median, 3, Color.Red);
        AtlantisPainter.paintCircle(median, 5, Color.Red);
        AtlantisPainter.paintCircle(median, 7, Color.Red);
        
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
        double minTiles = 1.8;
        double maxTiles = minTiles + 2.1;
        APosition runTo = null;

        // =========================================================
        
        while (false && minTiles <= maxTiles) {
            double vectorX = runAwayFrom.getX() - unit.getPosition().getX();
            double vectorY = runAwayFrom.getY() - unit.getPosition().getY();

            double vectorLength = unit.distanceTo(runAwayFrom);
            double ratio = minTiles / vectorLength;

            // Apply opposite 2D vector
//            runTo = new APosition(
//                    (int) (unit.getPosition().getX() - ratio * xDirectionToUnit),
//                    (int) (unit.getPosition().getY() - ratio * yDirectionToUnit)
//            );

            double angle = 40.5;
            double rotatedX = (vectorX * Math.cos(angle)) - (vectorY * Math.sin(angle));
            double rotatedY = (vectorX * Math.sin(angle)) + (vectorY * Math.cos(angle));
            vectorX = rotatedX;
            vectorY = rotatedY;

            runTo = new APosition(
                    (int) (unit.getPosition().getX() - ratio * vectorX),
                    (int) (unit.getPosition().getY() - ratio * vectorY)
            );
            
            // === Ensure position is in bounds ========================================
            
            runTo = runTo.makeValid();

//            if (Atlantis.getBwapi().isBuildable(runTo.toTilePosition(), true) 
//                    && unit.hasPathTo(runTo.getPoint())
//                    && Atlantis.getBwapi().hasPath(unit.getPosition(), runTo)
//                    && BWTA.isConnected(unit.getPosition().toTilePosition(), runTo.toTilePosition())) {

//            System.out.println("Atlantis.getBwapi().isBuildable(runTo.toTilePosition(), true) = " + Atlantis.getBwapi().isBuildable(runTo.toTilePosition(), true));
//            System.out.println("unit.hasPathTo(runTo.getPoint()) = " + unit.hasPathTo(runTo.getPoint()));
            if (unit.hasPathTo(runTo) && AtlantisMap.isWalkable(runTo)) {
                break;
            } else {
                minTiles++;
            }
        }
        
        // === Check if the place isn't too close ==================
        // If it is, it probably means we're in the corner and that we should run even towards the enemy,
        // with the hope of getting out.
        
        if (runTo == null || unit.distanceTo(runTo) < (minTiles - 0.2)) {
            runTo = findRunPointAtAnyDirection(unit, runAwayFrom);
        }

        // =========================================================
        
        if (runTo != null) {
//            AtlantisPainter.paintLine(unit.getPosition(), runTo, Color.Blue);
            return runTo;
        }
        else {
            System.err.println("Couldn't find run position - pretty f'ed up (" 
                    + (runTo != null ? APosition.createFrom(runTo).distanceTo(unit) : "null") + ") ");
            return null;
        }
    }
    
    /**
     * Returns a place where run to, searching in all directions, which is walkable, inbounds and
     * most distant to given runAwayFrom position.
     */
    private static APosition findRunPointAtAnyDirection(AUnit unit, APosition runAwayFrom) {
        int minDistanceInTiles = 3;
        int tx = unit.getTileX();
        int ty = unit.getTileY();
        
        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                
                // Define point and make sure it's inbounds
                APosition potentialPosition = APosition.createFrom(
                        tx + dx * minDistanceInTiles / 2, ty + dy * minDistanceInTiles / 2
                ).makeValid();
                
                // If has path to given point, add it to the list of potential points
                if (unit.hasPathTo(potentialPosition)) {
                    potentialPositionsList.add(potentialPosition);
//                    AtlantisPainter.paintLine(unit.getPosition(), potentialPosition, Color.Grey);
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
        
        return bestPosition;
    }
    
    private static Units defineCloseEnemies(AUnit unit) {
        double radius = unit.getType().isVulture() ? 6.5 : 2;
        return Select.enemy().combatUnits().canAttack(unit, radius).units();
    }
    
    // === Getters ========================================
    
    public APosition getRunToPosition() {
        return runTo;
    }
    
    public boolean isRunning() {
        return runTo != null;
    }
    
}
