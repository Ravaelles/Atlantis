package atlantis.combat.micro;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.combat.AtlantisCombatEvaluator;
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
        
        // Define position to run to
        nextPositionToRunTo = getPositionAwayFrom(unit, nearestEnemy);
        
        // Remember the last time of the decision
        if (nextPositionToRunTo != null) {
            lastRunTime = AtlantisGame.getTimeFrames();
        }
        
        // =========================================================
        // Update tooltip
        
        if (nextPositionToRunTo != null) {
            updateRunTooltip();
        }
        else {
            unit.removeTooltip();
        }
        
        // =========================================================

        // Make unit run to the selected position
        if (nextPositionToRunTo != null && !nextPositionToRunTo.equals((Position) unit)) {
            unit.move(nextPositionToRunTo, false);
            updateRunTooltip();
            
            // If this is massive retreat, make all other units run as well
            if (AtlantisCombatEvaluator.evaluateSituation(unit) < 0.2) {
                notifyOurUnitsAroundToRunAsWell(unit, nearestEnemy);
            }
            
            return true;
        }
        
        return false;
    }

    /**
     *
     */
    public static Position getPositionAwayFrom(Unit unit, Position runAwayFrom) {
        if (unit == null || runAwayFrom == null) {
            return null;
        }
        
//        if (AtlantisGame.getTimeSeconds() <= 350) {
            return findPositionToRun_preferMainBase(unit, runAwayFrom);
//        }
//        else {
//            return findPositionToRun_dontPreferMainBase(unit, runAwayFrom);
//        }
    }

    /**
     * Every unit that is relatively close to the unit that wants to run, should run as well, otherwise
     * it might block the escape route.
     */
    private void notifyOurUnitsAroundToRunAsWell(Unit ourUnit, Unit nearestEnemy) {
        
        // Get all of our units that are close to this unit
        double radius = Math.min(
                0.8, 2 * (ourUnit.getType().getDimensionLeft() + ourUnit.getType().getDimensionRight()) / 32
        );
        Collection<Unit> ourUnitsNearby = SelectUnits.our().inRadius(radius, ourUnit).list();
        
        // Tell them to run as well, not to block our escape route
        for (Unit ourOtherUnit : ourUnitsNearby) {
            if (!ourOtherUnit.isRunning()) {
                if (!unit.isMeleeUnit()) {
                    ourOtherUnit.runFrom(null);
                }
//                ourOtherUnit.runFrom(nearestEnemy);
            }
        }
    }
    
    // =========================================================
    // Find position to run away
    
    /**
     * Running behavior which will make unit run toward main base.
     */
    private static Position findPositionToRun_preferMainBase(Unit unit, Position runAwayFrom) {
        Unit mainBase = SelectUnits.mainBase();
        if (mainBase != null) {
            if (mainBase.distanceTo(unit) > 5) {
                return mainBase;
//                return mainBase.translated(0, 3 * 64);
            }
        }
        
        return findPositionToRun_dontPreferMainBase(unit, runAwayFrom);
    }
    
    /**
     * Running behavior which will make unit run <b>NOT</b> toward main base, but <b>away from the enemy</b>.
     */
    private static Position findPositionToRun_dontPreferMainBase(Unit unit, Position runAwayFrom) {
        int howManyTiles = 6;
        int maxTiles = 9;
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
            
//            if (howManyTiles >= 8) {
                runTo = runTo.makeValid();
//            }

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
        if (!unit.isHoldingPosition()) {
            unit.isHoldingPosition();
        }
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

    private void updateRunTooltip() {
        String runTimer = String.format("%.1f", 
                    ((double) AtlantisRunManager.getHowManyFramesUnitShouldStillBeRunning(unit) / 30));
        unit.setTooltip("Run " + runTimer + "s");
    }
    
}
