package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 * Handles the global mission that is mission that affects the battle squad Alpha.
 */
public class Missions {

    /**
     * This is the mission for main battle squad forces. E.g. initially it will be DEFEND, then it should be
     * PREPARE (go near enemy) and then ATTACK.
     */
    private static Mission currentGlobalMission;

    public static final Mission ATTACK = new MissionAttack("Attack");
    public static final Mission CONTAIN = new MissionContain("Contain");
    public static final Mission DEFEND = new MissionDefend("Defend");
    public static final Mission UMT = new MissionUmt("UMT");

    // =========================================================
    
    /**
     * Takes care of current strategy.
     */
    public static void evaluateGlobalMission() {
        if (currentGlobalMission == null) {
            currentGlobalMission = getInitialMission();
        }
        
        // === Handle UMT ==========================================
        
        if (AGame.isUmtMode() || Select.mainBase() == null) {
            return;
        }

        // =========================================================
        if (currentGlobalMission != Missions.ATTACK) {
            if (shouldChangeMissionToAttack()) {
//                if (AGame.playsAsTerran()) {
//                    if (Select.our().countUnitsOfType(AUnitType.UnitTypes.Terran_Medic) < 4) {
//                        return;
//                    }
//                }
                currentGlobalMission = Missions.ATTACK;
            }
        } else if (shouldChangeMissionToContain()) {
            currentGlobalMission = Missions.CONTAIN;
        }
    }

    // =========================================================
    
    /**
     * Defines how many military units we should have before pushing forward towards the enemy.
     */
    private static int defineMinUnitsToStrategicallyAttack() {

        // We're TERRAN
        if (AGame.isPlayingAsTerran()) {
            return 18;
        } // =========================================================
        // We're PROTOSS
        else if (AGame.isPlayingAsProtoss()) {
            return 18;
        } // =========================================================
        // We're ZERG
        else {
            return 3;
        }
    }

    private static boolean shouldChangeMissionToAttack() {
        
        // === Terran ========================================
        
        if (AGame.isPlayingAsTerran()) {
            if (Select.ourTanks().count() <= 4 && Select.ourCombatUnits().count() <= 30) {
                return false;
            }
        }
        
        // =========================================================

        if (Select.ourCombatUnits().count() < defineMinUnitsToStrategicallyAttack()) {
            return false;
        }

        return true;
    }

    private static boolean shouldChangeMissionToContain() {
        int ourCombatUnits = Select.ourCombatUnits().count();

        if (AGame.isPlayingAsTerran()) {
            return ourCombatUnits <= 13;
        } else if (AGame.isPlayingAsProtoss()) {
            return ourCombatUnits <= 8;
        } if (AGame.isPlayingAsZerg()) {
            return ourCombatUnits <= 8;
        }

        return true;
    }

    // =========================================================
    
    public static Mission getInitialMission() {
        
        // === Handle UMT ==========================================
        
        if (AGame.isUmtMode() || Select.mainBase() == null) {
//            return Missions.UMT;
            return Missions.ATTACK;
        }
        
        // =========================================================
        
//        return Missions.DEFEND;
//        return Missions.ATTACK;
        return Missions.CONTAIN;
    }

    /**
     * Global mission is the military stance that all non-special battle squads should follow and it should
     * always correspond to the mission of our main Alpha battle squad.
     */
    public static Mission globalMission() {
//        return Missions.ATTACK;

        if (currentGlobalMission == null) {
            currentGlobalMission = getInitialMission();
        }

        return currentGlobalMission;
    }

    public static boolean isGlobalMissionDefend() {
        return globalMission().isMissionDefend();
    }

    public static boolean isGlobalMissionContain() {
        return globalMission().isMissionContain();
    }

    public static boolean isGlobalMissionAttack() {
        if (AGame.isUmtMode()) {
            return true;
        }

        return globalMission().isMissionAttack();
    }

    public static void forceMissionAttack() {
        currentGlobalMission = Missions.ATTACK;
    }

    public static Mission fromString(String mission) {
        switch (mission.toUpperCase()) {
            case "ATTACK" : return ATTACK;
            case "CONTAIN" : return CONTAIN;
            case "DEFEND" : return DEFEND;
            default : AGame.exit("Invalid mission: " + mission); return null;
        }
    }
}
