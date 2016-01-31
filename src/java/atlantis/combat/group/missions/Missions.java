package atlantis.combat.group.missions;

import atlantis.AtlantisGame;
import atlantis.wrappers.SelectUnits;

/**
 * All group missions allowed.
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

    public static Mission defaultMission() {
        return Missions.DEFEND;
//        return Missions.ATTACK;
    }
    
    /**
     * Takes care of current strategy.
     */
    public static void handleGlobalMission() {
        if (currentGlobalMission == null) {
            currentGlobalMission = defaultMission();
        }
        
        // =========================================================
        
        if (currentGlobalMission == Missions.DEFEND) {
            if (SelectUnits.ourCombatUnits().count() >= defineMinUnitsToForFirstAttack()) {
                currentGlobalMission = Missions.ATTACK;
            }
        }
        else if (currentGlobalMission == Missions.ATTACK) {
            if (AtlantisGame.getTimeSeconds() > 350 && SelectUnits.ourCombatUnits().count() <= 5) {
                currentGlobalMission = Missions.DEFEND;
            }
        }
    }
    
    // =========================================================
    
    private static int defineMinUnitsToForFirstAttack() {
        if (AtlantisGame.isEnemyProtoss()) {
            return 4;
        }
        else if (AtlantisGame.isEnemyTerran()) {
            return 9;
        }
        else {
            return 9;
        }
    }
    
    // =========================================================

    public static Mission getCurrentGlobalMission() {
        return currentGlobalMission;
    }

}
