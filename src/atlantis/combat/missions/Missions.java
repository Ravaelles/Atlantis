package atlantis.combat.missions;

import atlantis.combat.advance.leader.CurrentFocusChoke;
import atlantis.combat.missions.attack.MissionAttack;
import atlantis.combat.missions.contain.MissionContain;
import atlantis.combat.missions.defend.MissionDefend;
import atlantis.combat.missions.defend.protoss.sparta.Sparta;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

/**
 * Handles the global mission that is mission that affects the battle squad Alpha.
 */
public class Missions {

    public static final Mission ATTACK = new MissionAttack();
    public static final Mission CONTAIN = new MissionContain();
    public static final Mission DEFEND = new MissionDefend();
    public static final Mission SPARTA = new Sparta();

    /**
     * This is the mission for main battle squad forces. E.g. initially it will be DEFEND, then it should be
     * PREPARE (go near enemy) and then ATTACK.
     */
    private static Mission currentGlobalMission = null;

    private static int lastMissionChanged = 0;
    private static int lastMissionEnforcedAt = -1;

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
            setGlobalMissionTo(initialMission(), "Initial mission");
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

    public static boolean isGlobalMissionContain() {
        return globalMission().isMissionContain();
    }

    public static boolean isGlobalMissionAttack() {
        if (AGame.isUms()) return true;

        return globalMission().isMissionAttack();
    }

    private static boolean enforceGlobalMission(Mission mission, String reason) {
        lastMissionEnforcedAt = A.now();
        setGlobalMissionTo(mission, reason);
        return true;
    }

    public static boolean forceGlobalMissionAttack(String reason) {
        return enforceGlobalMission(ATTACK, reason);
    }

    public static boolean forceGlobalMissionDefend(String reason) {
        return enforceGlobalMission(DEFEND, reason);
    }

    public static boolean forceGlobalMissionContain(String reason) {
        return enforceGlobalMission(CONTAIN, reason);
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

        if (AGame.isUms() || Select.main() == null) {
            return Missions.ATTACK;
        }

        // =========================================================

//        if (true) return Missions.DEFEND;

        // =========================================================

        if (OurStrategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
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
            case "CONTAIN":
                return CONTAIN;
            case "DEFEND":
                return DEFEND;
//            default : AGame.exit("Invalid mission: " + mission); return null;
            default:
                return null;
        }
    }

    public static void setGlobalMissionTo(Mission mission, String reason) {
        if (mission == null) {
            ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("Setting mission to null, ignore.");
            return;
        }

        if (A.isUms()) mission = ATTACK;

        if (mission.isMissionDefend()) {
            mission = MissionChanger.defendOrSpartaMission();
        }

        if (mission.equals(currentGlobalMission)) return;

        if (mission.isMissionDefend()) {
            CurrentFocusChoke.resetChoke();
        }

        Alpha.get().setMission(mission);

//        System.err.println("NEW MISSION " + mission.name() + " AT " + A.minSec() + ": " + reason);
        lastMissionChanged = A.now();
        currentGlobalMission = mission;

        if (A.now() > 50) {
//            if (mission.isMissionDefend()) {
//                throw new RuntimeException("DEF?!?");
//            }
//            if (mission.isMissionContain()) {
//                throw new RuntimeException("CHange to contain?!?");
//            }

//            if (MissionChanger.DEBUG) {
            A.println("MISSION @" + A.minSec() + " TO " + mission.name() + ": " + reason + " - " + mission.focusPoint());
//                A.printStackTrace("Changing mission to " + mission);
//            }
            MissionHistory.missionHistory.add(currentGlobalMission != null ? currentGlobalMission : mission);
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
