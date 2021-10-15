package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.units.Select;

/**
 * Handles the global mission that is mission that affects the battle squad Alpha.
 */
public class Missions {

    /**
     * This is the mission for main battle squad forces. E.g. initially it will be DEFEND, then it should be
     * PREPARE (go near enemy) and then ATTACK.
     */
    protected static Mission currentGlobalMission;

    public static final Mission ATTACK = new MissionAttack();
    public static final Mission CONTAIN = new MissionContain();
    public static final Mission DEFEND = new MissionDefend();
//    public static final Mission UMS = new MissionUms();

    // =========================================================

    /**
     * Global mission is the military stance that all non-special battle squads should follow and it should
     * always correspond to the mission of our main Alpha battle squad.
     */
    public static Mission globalMission() {
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
        if (AGame.isUmsMode()) {
            return true;
        }

        return globalMission().isMissionAttack();
    }

    public static Mission getInitialMission() {

        // === Handle UMS ==========================================

        if (AGame.isUmsMode() || Select.mainBase() == null) {
//            return Missions.UMS;
            return Missions.ATTACK;
        }

        // =========================================================

//        return Missions.DEFEND;
        return Missions.ATTACK;
//        return Missions.CONTAIN;
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
