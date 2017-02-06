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
import atlantis.units.missions.UnitMission;
import atlantis.units.missions.UnitMissions;
import atlantis.wrappers.APosition;
import bwapi.Color;
import bwapi.Position;
import bwta.BaseLocation;
import bwta.Region;

/**
 * This is the mission that is best used in UMT maps.
 */
public class MissionUmt extends Mission {

    public MissionUmt(String name) {
        super(name);
    }

    // =========================================================
    @Override
    public boolean update(AUnit unit) {
//        APosition focusPoint = getFocusPoint();
        AUnit enemyToAttack = null;
        APosition positionToAttack = null;
        
        // === Define unit that will be center of our army =================
        AUnit flagshipUnit = Select.ourCombatUnits().first();
        if (flagshipUnit == null) {
            unit.setTooltip("No flagship unit found");
            return false;
        }

        // === Return closest enemy ========================================
        AUnit nearestEnemy = Select.enemy().nearestTo(flagshipUnit);
//        System.out.println(nearestEnemy);
        if (nearestEnemy != null) {
            enemyToAttack = nearestEnemy;
//            System.out.println("    dist: " + nearestEnemy.distanceTo(unit));
            unit.setTooltip("#UMT:Attack!");
            return unit.attack(enemyToAttack, UnitMissions.ATTACK_UNIT);
        }

        // === Return location to go to ====================================
        
        positionToAttack = AtlantisMap.getNearestUnexploredRegion(flagshipUnit.getPosition());
        if (positionToAttack != null) {
            unit.setTooltip("#UMT:Explore");
            return unit.attack(positionToAttack, UnitMissions.EXPLORE);
        }
        
        // === Either attack a unit or go forward ==========================
        
//        if (enemyToAttack != null) {
//            unit.setTooltip("#UMT:Attack!");
//            return unit.attack(enemyToAttack, UnitMissions.ATTACK_UNIT);
//        }
//        else if (positionToAttack != null) {
//            unit.setTooltip("#UMT:Explore");
//            return unit.attack(positionToAttack, UnitMissions.EXPLORE);
//        }
//        else {
//        }
//
        System.err.println("UMT action: no mission action");
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
