package atlantis.combat.squad.missions;

import atlantis.AGame;
import atlantis.debug.APainter;
import atlantis.information.AMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.wrappers.APosition;
import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwta.Chokepoint;

public class MissionDefend extends Mission {
    
    private static MissionDefend instance;
    
    // =========================================================

    protected MissionDefend(String name) {
        super(name);
        instance = this;
    }
    
    // =============================================================
    
    @Override
    public boolean update(AUnit unit) {
        
        // === Handle UMT ==========================================
        
        if (AGame.isUmtMode()) {
            return false;
        }

        // === Load infantry into bunkers ==========================
        
        if (TerranInfantryManager.tryLoadingInfantryIntoBunkerIfPossible(unit)) {
            unit.setTooltip("GTFInto bunker!");
            return true;
        }
        
        // =========================================================
        
        APosition focusPoint = getFocusPoint();
        APainter.paintLine(unit, focusPoint, Color.Purple);
        
        if (focusPoint == null) {
            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
//            return false;
        }

        // =========================================================
        // Too close to the chokepoint
        else if (isCriticallyCloseToFocusPoint(unit, focusPoint)) {
            boolean result = unit.moveAwayFrom(focusPoint, 0.5);
            if (result) {
                unit.setTooltip("Too close (" + unit.distanceTo(focusPoint) + ")");
                return true;
            }
            else {
                unit.setTooltip("FAILED Too close");
            }
        }
        
        // =========================================================
        // Unit is quite close to the choke point
        else if (isCloseEnoughToFocusPoint(unit, focusPoint)) {

            // Too many stacked units
            if (isTooManyUnitsAround(unit, focusPoint)) {
                if (unit.isMoving()) {
                    unit.setTooltip("Hold");
                    unit.holdPosition();
                    return true;
                }
            } 
            
            // Everything is okay, be here
            else {
                if (unit.type().isTank() && !unit.isSieged()) {
                    unit.siege();
                    return true;
                }
                else {
                    unit.holdPosition();
                    unit.setTooltip("Hold");
                    return true;
                }
            }
        } 
        
        // =========================================================
        // Unit is far from choke point
        else {
            unit.setTooltip("Positioning");
            if (unit.distanceTo(focusPoint) > 3) {
                unit.move(focusPoint, UnitActions.MOVE);
                return true;
            }
        }
        
        unit.setTooltip("Defend");
        return false;
    }

    // =========================================================
    
    /**
     * AUnit will go towards important choke point near main base.
     */
    private boolean moveUnitIfNeededNearChokePoint(AUnit unit) {
        return false;
    }

    private boolean isTooManyUnitsAround(AUnit unit, APosition focusPoint) {
        return Select.ourCombatUnits().inRadius(1.0, unit).count() >= 3;
    }

    private boolean isCloseEnoughToFocusPoint(AUnit unit, APosition focusPoint) {
        if (unit == null || focusPoint == null) {
            return false;
        }
        

        // Bigger this value is, farther from choke will units stand
//        double unitShootRangeExtra = +0.3;

        // Distance to the center of choke point.
        double distToChoke = unit.distanceTo(focusPoint);
        
        return distToChoke < 3 + Select.ourCombatUnits().inRadius(3, unit).count() / 6;
//
//        // How far can the unit shoot
//        double unitShootRange =  unit.getWeaponRangeGround();
//
//        // Define max allowed distance from choke point to consider "still close"
//        double maxDistanceAllowed = unitShootRange + unitShootRangeExtra;
//
//        return distToChoke <= maxDistanceAllowed;
    }

    private boolean isCriticallyCloseToFocusPoint(AUnit unit, APosition focusPoint) {
        if (unit == null || focusPoint == null) {
            return false;
        }

        // Distance to the center of choke point.
        double distToChoke = unit.distanceTo(focusPoint);

        // Can't be closer than X from choke point
        if (distToChoke > 0.01 && distToChoke <= 1.2) {
            return true;
        }

//        // Bigger this value is, farther from choke will units stand
//        double standFarther = 1;
//
//        // How far can the unit shoot (in build tiles)
//        double unitShootRange = unit.getWeaponRangeGround();
//
//        // Define max distance
//        double maxDistance = unitShootRange + standFarther;
//
//        return distToChoke <= maxDistance;

        return false;
    }

    // =========================================================

    @Override
    public APosition getFocusPoint() {
        
        // === Handle UMT ==========================================
        
        if (AGame.isUmtMode()) {
            return null;
        }
        
        // =========================================================
        
//        if (Select.ourBases().count() <= 1) {
//            return APosition.create(AtlantisMap.getChokepointForMainBase().getCenter());
//        }
//        else {
            return APosition.create(AMap.getChokepointForNaturalBase().getCenter());
//        }
    }
    
    // =========================================================
    
    public static MissionDefend getInstance() {
        return instance;
    }

}
