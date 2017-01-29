package atlantis.combat.squad.missions;

import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.missions.UnitMissions;
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
        Chokepoint chokepoint = getFocusPoint();
        if (chokepoint == null) {
            System.err.println("Couldn't define choke point.");
            return false;
        }

        // =========================================================
        // Too close to the chokepoint
        else if (isCriticallyCloseToChokePoint(unit, chokepoint)) {
            unit.moveAwayFrom(chokepoint.getCenter(), 0.4);
            unit.setTooltip("Too close");
            return true;
        }
        
        // =========================================================
        // Unit is quite close to the choke point
        else if (isCloseEnoughToChokePoint(unit, chokepoint)) {

            // Too many stacked units
            if (isTooManyUnitsAround(unit, chokepoint)) {
                if (unit.isMoving()) {
                    unit.setTooltip("Stop");
                    unit.holdPosition();
                    return true;
                }
            } 
        } 
        
        // =========================================================
        // Unit is far from choke point
        else {
            unit.setTooltip("Positioning");
            Position center = chokepoint.getCenter();
            if (unit.distanceTo(center) > 2) {
                unit.move(center, UnitMissions.MOVE);
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

    private boolean isTooManyUnitsAround(AUnit unit, Chokepoint chokepoint) {
        return Select.ourCombatUnits().inRadius(1.0, unit.getPosition()).count() >= 3;
    }

    private boolean isCloseEnoughToChokePoint(AUnit unit, Chokepoint chokepoint) {
        if (unit == null || chokepoint == null) {
            return false;
        }

        // Bigger this value is, further from choke will units stand
        double unitShootRangeExtra = +0.3;

     // Distance to the center of choke point. TODO: check whether getWidth()/100.0f has the same effect of getRadiusInTiles
        double distToChoke = unit.distanceTo(chokepoint.getCenter()) - chokepoint.getWidth()/100.0f;	// getRadiusInTiles()

        // How far can the unit shoot
        double unitShootRange =  unit.getType().getGroundWeapon().maxRange() / 32; //getShootRangeGround();

        // Define max allowed distance from choke point to consider "still close"
        double maxDistanceAllowed = unitShootRange + unitShootRangeExtra;

        return distToChoke <= maxDistanceAllowed;
    }

    private boolean isCriticallyCloseToChokePoint(AUnit unit, Chokepoint chokepoint) {
        if (unit == null || chokepoint == null) {
            return false;
        }

        // Distance to the center of choke point. TODO: check whether getWidth()/100.0f has the same effect of getRadiusInTiles
        double distToChoke = unit.distanceTo(chokepoint.getCenter()) - chokepoint.getWidth()/100.0f;	// getRadiusInTiles()

        // Can't be closer than X from choke point
        if (distToChoke <= 4.2) {
            return true;
        }

        // Bigger this value is, further from choke will units stand
        double standFurther = 1;

        // How far can the unit shoot (in build tiles)
        double unitShootRange = unit.getType().getGroundWeapon().maxRange() / TilePosition.SIZE_IN_PIXELS; //getShootRangeGround();

        // Define max distance
        double maxDistance = unitShootRange + standFurther;

        return distToChoke <= maxDistance;
    }

    // =========================================================
    
    public static Chokepoint getFocusPoint() {
        if (Select.ourBases().count() <= 1) {
            return AtlantisMap.getMainBaseChokepoint();
        }
        else {
            return AtlantisMap.getNaturalBaseChokepoint();
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
