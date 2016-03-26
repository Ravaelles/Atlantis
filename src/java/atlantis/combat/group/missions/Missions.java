package atlantis.combat.group.missions;

import atlantis.AtlantisGame;
import atlantis.combat.micro.terran.TerranMedic;
import atlantis.wrappers.Select;
import bwapi.UnitType;

/**
 * Handles the global mission that is mission that affects the battle  group Alpha.
 */
public class Missions {
    
    /**
     * This is the mission for main battle group forces. E.g. initially it will be DEFEND, then it should be
     * PREPARE (go near enemy) and then ATTACK.
     */
    private static Mission currentGlobalMission;

    public static final Mission DEFEND = new MissionDefend("Defend");
    public static final Mission PREPARE = new MissionPrepare("Prepare");
    public static final Mission ATTACK = new MissionAttack("Attack");
    
    // =========================================================
    
    /**
     * Takes care of current strategy.
     */
    public static void handleGlobalMission() {
        if (currentGlobalMission == null) {
            currentGlobalMission = getInitialMission();
        }
        
        // =========================================================
        
        if (currentGlobalMission == Missions.DEFEND) {
            if (Select.ourCombatUnits().count() >= defineMinUnitsToForFirstAttack()) {
//                if (AtlantisGame.playsAsTerran()) {
//                    if (Select.our().countUnitsOfType(UnitType.UnitTypes.Terran_Medic) < 4) {
//                        return;
//                    }
//                }
                currentGlobalMission = Missions.ATTACK;
            }
        }
        else if (currentGlobalMission == Missions.ATTACK) {
            if (AtlantisGame.getTimeSeconds() > 350 && Select.ourCombatUnits().count() <= 5) {
                currentGlobalMission = Missions.DEFEND;
            }
        }
    }
    
    // =========================================================
    
    public static Mission getInitialMission() {
        return Missions.DEFEND;
//        return Missions.ATTACK;
    }
    
    /**
     * Defines how many military units we should have before pushing forward towards the enemy.
     */
    private static int defineMinUnitsToForFirstAttack() {
        if (AtlantisGame.isEnemyProtoss()) {
            return 4;
        }
        else if (AtlantisGame.isEnemyTerran()) {
            return 1;
        }
        else {
            return 2;
        }
    }
    
    // =========================================================

    /**
     * Global mission is the military stance that all non-special battle groups should follow and it
     * should always correspond to the mission of our main Alpha battle group.
     */
    public static Mission getCurrentGlobalMission() {
        return currentGlobalMission;
    }

}
