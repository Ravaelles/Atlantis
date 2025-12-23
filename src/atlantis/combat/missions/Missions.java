package atlantis.combat.missions;

import atlantis.combat.missions.attack.MissionAttack;
import atlantis.combat.missions.defend.MissionDefend;
import atlantis.combat.missions.defend.protoss.sparta.Sparta;
import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.Strategy;

/**
 * Handles the global mission that is mission that affects the battle squad Alpha.
 */
public class Missions {

    public static final Mission ATTACK = new MissionAttack();
    public static final Mission DEFEND = new MissionDefend();
    public static final Mission SPARTA = new Sparta();

    /**
     * This is the mission for main battle squad forces. E.g. initially it will be DEFEND, then it should be
     * PREPARE (go near enemy) and then ATTACK.
     */
    protected static Mission currentGlobalMission = null;

    protected static int lastMissionChanged = 0;
    protected static int lastMissionEnforcedAt = -1;

    // =========================================================

    /**
     * Global mission is the military stance that all non-special battle squads should follow and it should
     * always correspond to the mission of our main Alpha battle squad.
     */
    public static Mission globalMission() {
        if (A.isUms()) {
            return DEFEND;
//            return ATTACK;
        }

        if (currentGlobalMission == null) {
            MissionChanger.setGlobalMissionTo(initialMission(), "Initial mission");
        }

        return currentGlobalMission;
    }

    public static boolean isGlobalMissionDefend() {
        return globalMission().isMissionDefend();
    }

    public static boolean isGlobalMissionSparta() {
        return globalMission().isMissionSparta();
    }

    public static boolean isGlobalMissionDefendOrSparta() {
        return isGlobalMissionDefend() || isGlobalMissionSparta();
    }

    public static boolean isGlobalMissionAttack() {
        if (AGame.isUms()) return true;

        return globalMission().isMissionAttack();
    }

    private static boolean enforceGlobalMission(Mission mission, String reason) {
        lastMissionEnforcedAt = A.now();
        MissionChanger.setGlobalMissionTo(mission, reason);
        return true;
    }

    public static boolean forceGlobalMissionAttack(String reason) {
        return enforceGlobalMission(ATTACK, reason);
    }

    public static boolean forceGlobalMissionDefend(String reason) {
        return enforceGlobalMission(DEFEND, reason);
    }

    public static boolean forceGlobalMissionSparta(String reason) {
        return enforceGlobalMission(SPARTA, reason);
    }

    public static boolean forceGlobalMissionFromBuildOrder(Mission mission, String buildOrderMission) {
        System.err.println(A.minSec() + ": FORCE GLOBAL MISSION FROM BUILD ORDER: " + mission + ", supply: " + A.supplyUsed());
        return enforceGlobalMission(mission, buildOrderMission);
    }

    public static Mission initialMission() {

        // === Handle UMS ==========================================

        if (AGame.isUms() || Env.isTesting()) {
            return Missions.ATTACK;
        }

        // =========================================================

//        if (true) return Missions.DEFEND;

        // =========================================================

        if (Strategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
            return Missions.ATTACK;
        }

//        if (Enemy.zerg()) {
//            return Missions.DEFEND;
//        }

//        return MissionChanger.defendOrSpartaMission();
        return Missions.ATTACK;
//        return Missions.CONTAIN;
    }

    public static Mission fromString(String mission) {
        mission = mission.toUpperCase().replace("MISSION=", "");
        mission = mission.toUpperCase().replace("MISSION:", "");
        switch (mission) {
            case "ATTACK":
                return ATTACK;
            case "DEFEND":
                return DEFEND;
//            default : AGame.exit("Invalid mission: " + mission); return null;
            default:
                return null;
        }
    }

    public static int lastMissionChangedAgo() {
        return A.ago(lastMissionChanged);
    }

    public static double lastMissionChangedSecondsAgo() {
        return A.secondsAgo(lastMissionChanged);
    }

//    public static boolean recentlyChangedMission() {
//        return lastMissionChangedAgo() <= 30 * 6;
//    }

    public static int historyCount() {
        return MissionHistory.missionHistory.size();
    }

//    public static Mission prevMission() {
//        if (MissionHistory.missionHistory.size() >= 2) {
//            return MissionHistory.missionHistory.get(MissionHistory.missionHistory.size() - 2);
//        }
//        else {
//            return null;
//        }
//    }

    public static boolean isFirstMission() {
        return MissionHistory.missionHistory.size() == 1;
    }

    public static double lastMissionEnforcedSecondsAgo() {
        return A.secondsAgo(lastMissionEnforcedAt);
    }
}
