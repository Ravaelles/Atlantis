package atlantis.combat.missions;

import atlantis.map.AMap;
import atlantis.map.ARegion;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import bwta.Region;

/**
 * This is the mission that is best used in UMT maps.
 */
public class MissionUmt extends Mission {

    private static APosition _tempFocusPoint = null;
    private static AUnit flagshipUnit = null;

    // =========================================================

    protected MissionUmt(String name) {
        super(name, null, null);
    }
    
    // =========================================================
    
    @Override
    public boolean update(AUnit unit) {
        System.out.println("UMT mission");

        // DISABLED
//        if (1 < 2) {
//            return false;
//        }
        
//        if (unit.isJustShooting() || !unit.isReadyToShoot()) {
//        if (unit.isJustShooting()) {
//            return true;
//        }

        // =========================================================
        
//        APosition focusPoint = focusPoint();
        AUnit enemyToEngage = null;
        APosition explorePosition = null;

        // === Define unit that will be center of our army =================
        
        flagshipUnit = Select.ourCombatUnits().first();
        if (flagshipUnit == null) {
            unit.setTooltip("No flagship unit found");
            return false;
        }

        // === Stick close to flagship unit ================================
        
        boolean isFlagship = flagshipUnit.equals(unit);
        double distanceToFlagship = flagshipUnit.distanceTo(unit.getPosition());

        if (isFlagship) {
            if (Select.ourCombatUnits().inRadius(2.5, unit).count() == 0) {
//                AUnit nearestUnit = Select.ourCombatUnits().exclude(unit).nearestTo(flagshipUnit);
                AUnit otherUnit = Select.ourCombatUnits().exclude(unit).first();
                if (otherUnit != null) {
                    unit.setTooltip("#WaitForDaKing");
                    unit.move(otherUnit.getPosition(), UnitActions.MOVE);
                    return true;
                }
            }
        } else {
            if (distanceToFlagship > 7) {
                if (distanceToFlagship > 6) {
                    unit.move(flagshipUnit.getPosition(), UnitActions.MOVE);
                    unit.setTooltip("#ToFlagship");
                    return true;
                } else {
                    if (Select.ourCombatUnits().inRadius(2, unit).count() == 0) {
                        unit.setTooltip("#Closer");
                        unit.move(flagshipUnit.getPosition(), UnitActions.MOVE);
                        return true;
                    }
                }
            } else {
                int veryCloseInRadius = Select.ourCombatUnits().inRadius(4, unit).count();
                if (veryCloseInRadius > 0) {
                    Select<?> inRadius = Select.ourCombatUnits().inRadius(2 / veryCloseInRadius, unit);
                    if (inRadius.count() > 0 && unit.moveAwayFrom(inRadius.nearestTo(unit).getPosition(), 0.3)) {
                        unit.setTooltip("#Separate");
                        return true;
                    }
                }
            }
        }

        // === Return location to go to ====================================
        
        ARegion nearestUnexploredRegion = AMap.getNearestUnexploredRegion(flagshipUnit.getPosition());
        explorePosition = (nearestUnexploredRegion != null
                ? APosition.create(nearestUnexploredRegion.getCenter()) : null);
        if (!unit.isMoving() && explorePosition != null && explorePosition.distanceTo(unit) > 2.5) {
            unit.setTooltip("#Explore" + (isFlagship ? "Flag" : ""));
            return unit.move(explorePosition, UnitActions.EXPLORE);
        }

        // === Return closest enemy ========================================
        
        AUnit nearestEnemy = Select.enemy().nearestTo(flagshipUnit);
        if (nearestUnexploredRegion == null && nearestEnemy != null && unit.hasPathTo(nearestEnemy.getPosition())) {
            enemyToEngage = nearestEnemy;
            unit.setTooltip("#Engage");
            
            if (unit.hasRangeToAttack(enemyToEngage, 0)) {
                return unit.attackUnit(enemyToEngage);
            }
            else {
                return unit.move(enemyToEngage.getPosition(), UnitActions.MOVE_TO_ENGAGE);
            }
        }

        // === Go to nearest unexplored position ===========================
        
//        if (_tempFocusPoint != null) {
//            System.out.println(_tempFocusPoint + ": dist " + _tempFocusPoint.distanceTo(unit));
//        }
        if (_tempFocusPoint == null || _tempFocusPoint.distanceTo(unit) < 3) {
            _tempFocusPoint = focusPoint();

            if (_tempFocusPoint != null && _tempFocusPoint.distanceTo(unit) > 1.5) {
                unit.setTooltip(isFlagship ? "Hernan Cortes" : "Companero");
                return unit.move(explorePosition, UnitActions.EXPLORE);
            }
        }

        // === Either attack a unit or go forward ==========================
//        if (enemyToAttack != null) {
//            unit.setTooltip("#UMT:Attack!");
//            return unit.attack(enemyToAttack, UnitActions.ATTACK_UNIT);
//        }
//        else if (positionToAttack != null) {
//            unit.setTooltip("#UMT:Explore");
//            return unit.attack(positionToAttack, UnitActions.EXPLORE);
//        }
//        else {
//        }
//
//        System.err.println("UMT action: no mission action");
        unit.setTooltip("#SeenAllInMyLife");
        return false;
    }

    // =========================================================
    /**
     * Returns the <b>position</b> (not the unit itself) where we should point our units to in hope because as
     * far as we know, the enemy is/can be there and it makes sense to attack in this region.
     */
//    public static APosition focusPoint() {
//
//        // === Define unit that will be center of our army =================
//        AUnit flagshipUnit = Select.ourCombatUnits().first();
//        if (flagshipUnit == null) {
//            return null;
//        }
//
//        // === Return closest enemy ========================================
//        AUnit nearestEnemy = Select.enemy().nearestTo(flagshipUnit);
//        if (nearestEnemy != null) {
//            return nearestEnemy.getPosition();
//        }
//
//        // === Return location to go to ====================================
//        return AtlantisMap.getNearestUnexploredRegion(flagshipUnit.getPosition());
//    }
    
    @Override
    public APosition focusPoint() {
        return null;
//        _tempFocusPoint = AMap.getNearestUnexploredAccessiblePosition(flagshipUnit.getPosition());
//        return _tempFocusPoint;
    }
    
    // =========================================================
    
//    public static MissionUmt getInstance() {
//        return instance;
//    }

    public static AUnit getFlagshipUnit() {
        return flagshipUnit;
    }
    
}
