package atlantis.combat.squad.missions;

import atlantis.AtlantisGame;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.wrappers.APosition;
import bwapi.Position;
import bwapi.TilePosition;
import bwta.Chokepoint;


public class MissionDefend extends Mission {

    public MissionDefend(String name) {
        super(name);
    }
    
    // =========================================================

    @Override
    public boolean update(AUnit unit) {
        
        // === Handle UMT ==========================================
        
        if (AtlantisGame.isUmtMode()) {
            return false;
        }
        
        // =========================================================
        
        APosition focusPoint = getFocusPoint();
        if (focusPoint == null) {
            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
//            return false;
        }

        // =========================================================
        // Too close to the chokepoint
        else if (isCriticallyCloseToFocusPoint(unit, focusPoint)) {
            unit.moveAwayFrom(focusPoint, 0.4);
            unit.setTooltip("Too close");
            return true;
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
        double unitShootRangeExtra = +0.3;

        // Distance to the center of choke point.
        double distToChoke = unit.distanceTo(focusPoint);

        // How far can the unit shoot
        double unitShootRange =  unit.getWeaponRangeGround();

        // Define max allowed distance from choke point to consider "still close"
        double maxDistanceAllowed = unitShootRange + unitShootRangeExtra;

        return distToChoke <= maxDistanceAllowed;
    }

    private boolean isCriticallyCloseToFocusPoint(AUnit unit, APosition focusPoint) {
        if (unit == null || focusPoint == null) {
            return false;
        }

        // Distance to the center of choke point.
        double distToChoke = unit.distanceTo(focusPoint);

        // Can't be closer than X from choke point
        if (distToChoke <= 4.2) {
            return true;
        }

        // Bigger this value is, farther from choke will units stand
        double standFarther = 1;

        // How far can the unit shoot (in build tiles)
        double unitShootRange = unit.getWeaponRangeGround();

        // Define max distance
        double maxDistance = unitShootRange + standFarther;

        return distToChoke <= maxDistance;
    }

    // =========================================================
    
    public static APosition getFocusPoint() {
        
        // === Handle UMT ==========================================
        
        if (AtlantisGame.isUmtMode()) {
            return null;
        }
        
        // =========================================================
        
        if (Select.ourBases().count() <= 1) {
            return APosition.createFrom(AtlantisMap.getMainBaseChokepoint().getCenter());
        }
        else {
            return APosition.createFrom(AtlantisMap.getNaturalBaseChokepoint().getCenter());
        }
    }

    /**
     * Do not interrupt unit if it is engaged in combat.
     */
//    @Override
//    protected boolean canIssueOrderToUnit(AUnit unit) {
//
//        // If unit has far more important actions than positioning, disallow any actions here.
//        if (unit.isAttacking() || unit.isStartingAttack() || unit.isRunning() 
//                || unit.isAttackFrame()) {
//            return false;
//        }
//
//        return true;
//    }
}
