package atlantis.combat.micro;

import atlantis.debug.AtlantisPainter;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.missions.UnitMissions;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APosition;
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
//        List<AUnit> closeEnemies = defineCloseEnemies(unit);
        Units closeEnemies = defineCloseEnemies(unit);
        
        // Define "center of gravity" for the set of enemies
        APosition median = closeEnemies.median();
        
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
        int minTiles = 2;
        int maxTiles = 4;
        APosition runTo = null;

        // =========================================================
        
        while (false && minTiles <= maxTiles) {
            double xDirectionToUnit = runAwayFrom.getX() - unit.getPosition().getX();
            double yDirectionToUnit = runAwayFrom.getY() - unit.getPosition().getY();

            double vectorLength = unit.distanceTo(runAwayFrom);
            double ratio = minTiles / vectorLength;

            // Add randomness of move if distance is big enough
            //        int xRandomness = howManyTiles > 3 ? (2 - AtlantisUtilities.rand(0, 4)) : 0;
            //        int yRandomness = howManyTiles > 3 ? (2 - AtlantisUtilities.rand(0, 4)) : 0;
            runTo = new APosition(
                    (int) (unit.getPosition().getX() - ratio * xDirectionToUnit),
                    (int) (unit.getPosition().getY() - ratio * yDirectionToUnit)
            );
//            System.out.println("      Run to: " + runTo + " / dist: " 
//                    + (runTo != null ? APosition.createFrom(runTo).distanceTo(unit) : "null"));

            
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
            
            break;
        }
        
        // === Check if the place isn't too close ==================
        // If it is, it probably means we're in the corner and that we should run even towards the enemy,
        // with the hope of getting out.
        
        if (runTo == null || unit.distanceTo(runTo) < (minTiles - 0.2)) {
            runTo = findLongDistanceRunPoint(unit, runAwayFrom);
        }

        // =========================================================
        
        if (runTo != null) {
            AtlantisPainter.paintLine(unit.getPosition(), runTo, Color.Blue);
            return runTo;
        }
        else {
            System.err.println("Couldn't find run position - pretty f'ed up (" 
                    + (runTo != null ? APosition.createFrom(runTo).distanceTo(unit) : "null") + ") ");
            return null;
        }
    }
    
    /**
     *
     */
    private static APosition findLongDistanceRunPoint(AUnit unit, APosition runAwayFrom) {
        int minDistanceInTiles = 4;
        int tx = unit.getTileX();
        int ty = unit.getTileY();
        
        // Build list of possible run positions, basically around the clock
        ArrayList<APosition> potentialPositionsList = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                
                // Define point and make sure it's inbounds
                APosition potentialPosition = APosition.createFrom(
                        tx + dx * minDistanceInTiles, ty + dy * minDistanceInTiles
                ).makeValid();
                
                // If has path to given point, add it to the list of potential points
                if (unit.hasPathTo(potentialPosition)) {
                    potentialPositionsList.add(potentialPosition);
                    AtlantisPainter.paintLine(unit.getPosition(), potentialPosition, Color.Grey);
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
    
//    private static int countNearbyUnits(Position position) {
//        int total = 0;
//        List<AUnit> unitsInRange = (List<AUnit>) Select.our().inRadius(6, position).listUnits();
//        for (AUnit unit : unitsInRange) {
//            if (!unit.isRunning()) {
//                total++;
//            }
//        }
//        return total;
//    }

    private static Units defineCloseEnemies(AUnit unit) {
//    private List<AUnit> defineCloseEnemies(AUnit unit) {
//        ArrayList<AUnit> closeEnemies = new ArrayList<>();
        return Select.enemy().combatUnits().canAttack(unit, 1.0).units();
    }
    
    // === Getters ========================================
    
    public APosition getRunToPosition() {
        return runTo;
    }
    
    public boolean isRunning() {
        return runTo != null;
    }
    
}
