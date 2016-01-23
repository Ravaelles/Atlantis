package atlantis.combat.micro;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.information.AtlantisMap;
import atlantis.wrappers.SelectUnits;
import java.util.Collection;
import jnibwapi.Position;
import jnibwapi.Unit;

/**
 * Handles best way of running from close enemies and information about the fact if given unit is running or
 * not.
 */
public class AtlantisRunning {

    private Unit unit;
    private Position nextPositionToRunTo = null;
    private int lastRunTime = -1;

    // =========================================================
    
    public AtlantisRunning(Unit unit) {
        super();
        this.unit = unit;
    }

    // =========================================================
    // Hi-level methods
    
    /**
     * Indicates that this unit should be running from given enemy unit.
     */
    public boolean runFrom(Unit nearestEnemy) {
//        int dx = 3 * (nearestEnemy.getPX() - unit.getPX());
//        int dy = 3 * (nearestEnemy.getPY() - unit.getPY());
        nextPositionToRunTo = getPositionAwayFrom(unit, nearestEnemy);

        if (nextPositionToRunTo != null && !nextPositionToRunTo.equals((Position) unit)) {
            unit.move(nextPositionToRunTo, false);
            lastRunTime = AtlantisGame.getTimeFrames();
            
            unit.setTooltip("Run " + String.format("%.1f", nextPositionToRunTo.distanceTo(unit)));
            notifyOurUnitsAroundToRunAsWell(unit, nearestEnemy);
            
            return true;
        }
        
        return false;
    }
    
    private void notifyOurUnitsAroundToRunAsWell(Unit ourUnit, Unit nearestEnemy) {
        
        // Get all of our units that are close to this unit
        Collection<Unit> ourUnitsNearby = SelectUnits.our().inRadius(0.8, ourUnit).list();
        
        // Tell them to run as well, not to block our escape route
        for (Unit ourOtherUnit : ourUnitsNearby) {
            if (!ourOtherUnit.isRunning()) {
                ourOtherUnit.runFrom(nearestEnemy);
            }
        }
    }

    public static Position getPositionAwayFrom(Unit unit, Position runAwayFrom) {
        if (unit == null || runAwayFrom == null) {
            return null;
        }
        int howManyTiles = 2;
        int maxTiles = 5;
        Position runTo = null;
        
        // =========================================================

        while (howManyTiles <= maxTiles) {
            double xDirectionToUnit = runAwayFrom.getPX() - unit.getPX();
            double yDirectionToUnit = runAwayFrom.getPY() - unit.getPY();

            double vectorLength = runAwayFrom.distanceTo(unit);
            double ratio = howManyTiles / vectorLength;

            // Add randomness of move if distance is big enough
            //        int xRandomness = howManyTiles > 3 ? (2 - RUtilities.rand(0, 4)) : 0;
            //        int yRandomness = howManyTiles > 3 ? (2 - RUtilities.rand(0, 4)) : 0;
            runTo = new Position(
                    (int) (unit.getPX() - ratio * xDirectionToUnit),
                    (int) (unit.getPY() - ratio * yDirectionToUnit)
            );
            
//            );
//            return runTo;
            
            if (howManyTiles >= 4) {
                runTo = runTo.makeValid();
            }

            if (Atlantis.getBwapi().isBuildable(runTo, true) && unit.hasPathTo(runTo)
                    & Atlantis.getBwapi().hasPath(unit, runTo)
                    && AtlantisMap.getMap().isConnected(unit, runTo)) {
                break;
            } else {
                howManyTiles++;
            }
        }
        
        // =========================================================
        
        if (runTo != null) {
            double dist = unit.distanceTo(runTo);
            if (dist >= 0.8 && dist <= maxTiles + 1) {
                return runTo;
            }
        }
        
        return SelectUnits.mainBase();
    }

    // =========================================================
    // Stop running
    
    public void stopRunning() {
        nextPositionToRunTo = null;
    }
    
    // =========================================================
    // Getters & Setters
    
    /**
     * Returns true if given unit is currently (this frame) running from an enemy.
     */
    public boolean isRunning() {
        return nextPositionToRunTo != null;
    }

    public Unit getUnit() {
        return unit;
    }

    /**
     * Returns the position where unit is running to (it's quite close to the unit, few tiles).
     */
    public Position getNextPositionToRunTo() {
        return nextPositionToRunTo;
    }

    public int getTimeSinceLastRun() {
        return AtlantisGame.getTimeFrames() - lastRunTime;
    }
    
}
