package atlantis.combat.squad.missions;

import atlantis.Atlantis;
import atlantis.AtlantisGame;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.information.AtlantisMap;
import atlantis.information.UnitData;
import atlantis.scout.AtlantisScoutManager;
import static atlantis.scout.AtlantisScoutManager.getUmtFocusPoint;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.missions.UnitAction;
import atlantis.units.missions.UnitActions;
import atlantis.wrappers.APosition;
import bwapi.Color;
import bwapi.Position;
import bwta.BaseLocation;
import bwta.Region;

/**
 * This is the mission that is best used in UMT maps.
 */
public class MissionUmt extends Mission {

    private static APosition _tempFocusPoint = null;
    
    // =========================================================
    
    public MissionUmt(String name) {
        super(name);
    }

    // =========================================================
    @Override
    public boolean update(AUnit unit) {
        if (unit.isJustShooting() || !unit.isReadyToShoot() || unit.isAttacking()) {
            return false;
        }
        
        // =========================================================
        
//        APosition focusPoint = getFocusPoint();
        AUnit engageEnemy = null;
        APosition explorePosition = null;

        // === Define unit that will be center of our army =================
        AUnit flagshipUnit = Select.ourCombatUnits().first();
        if (flagshipUnit == null) {
            unit.setTooltip("No flagship unit found");
            return false;
        }

        // === Stick close to flagship unit ================================
        
        boolean isFlagship = flagshipUnit.equals(unit);
        double distanceToFlagship = flagshipUnit.distanceTo(unit.getPosition());
        
        if (isFlagship) {
            if (Select.ourCombatUnits().inRadius(3, unit).count() == 0) {
                AUnit nearestUnit = Select.ourCombatUnits().nearestTo(flagshipUnit);
                if (nearestUnit != null) {
                    unit.move(nearestUnit.getPosition(), UnitActions.STICK_CLOSER);
                    return true;
                }
            }
        }
        else {
            if (distanceToFlagship > 5) {
                if (distanceToFlagship > 7) {
                    unit.move(flagshipUnit.getPosition(), UnitActions.STICK_CLOSER);
                    return true;
                }
                else {
                    if (Select.ourCombatUnits().inRadius(1.5, unit).count() == 0) {
                        unit.setTooltip("#Closer (" + ((int) flagshipUnit.distanceTo(unit)) + ")");
                        unit.move(flagshipUnit.getPosition(), UnitActions.STICK_CLOSER);
                        return true;
                    }
                }
            }
        }

        // === Return closest enemy ========================================
        AUnit nearestEnemy = Select.enemy().nearestTo(flagshipUnit);
//        System.out.println(nearestEnemy);
        if (nearestEnemy != null && unit.hasPathTo(nearestEnemy.getPosition())) {
            engageEnemy = nearestEnemy;
//            System.out.println("    dist: " + nearestEnemy.distanceTo(unit));
            unit.setTooltip("#Engage");
            return unit.move(engageEnemy.getPosition(), UnitActions.ENGAGE);
        }

        // === Return location to go to ====================================
        Region nearestUnexploredRegion = AtlantisMap.getNearestUnexploredRegion(flagshipUnit.getPosition());
        explorePosition = (nearestUnexploredRegion != null 
                ? APosition.createFrom(nearestUnexploredRegion.getCenter()) : null);
        if (explorePosition != null && explorePosition.distanceTo(unit) > 2.5) {
            unit.setTooltip("#Explore");
            return unit.move(explorePosition, UnitActions.EXPLORE);
        }
        
        // === Go to nearest unexplored position ===========================
        
        if (_tempFocusPoint == null || _tempFocusPoint.distanceTo(unit) < 3) {
            _tempFocusPoint = AtlantisMap.getNearestUnexploredAccessiblePosition(unit.getPosition());

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
//    public static APosition getFocusPoint() {
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
}
