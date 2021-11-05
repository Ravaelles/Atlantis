package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.ArrayList;

public class MissionChanger {

    protected static ArrayList<Mission> missionHistory = new ArrayList<>();

    // =========================================================

    /**
     * Takes care of current strategy.
     */
    public static void evaluateGlobalMission() {

        // === Handle UMS ==========================================

        if (AGame.isUms() || Select.mainBase() == null) {
            return;
        }

        // =========================================================

        if (Missions.isGlobalMissionAttack()) {
            MissionChangerWhenAttack.changeMissionIfNeeded();
        } else if (Missions.isGlobalMissionContain()) {
            MissionChangerWhenContain.changeMissionIfNeeded();
        } else if (Missions.isGlobalMissionDefend()) {
            MissionChangerWhenDefend.changeMissionIfNeeded();
        }
    }


    public static void notifyThatUnitRetreated(AUnit unit) {
        if (isFirstMission() && Missions.isGlobalMissionAttack()) {
            forceMissionContain();
        }
    }

    // =========================================================

    protected static void changeMissionTo(Mission newMission) {
        Missions.setGlobalMissionTo(newMission);
        missionHistory.add(newMission);
    }

    public static Mission prevMission() {
        if (missionHistory.size() >= 2) {
            return missionHistory.get(missionHistory.size() - 2);
        } else {
            return null;
        }
    }

    public static void forceMissionAttack() {
        Missions.setGlobalMissionAttack();
    }

    public static void forceMissionContain() {
        Missions.setGlobalMissionContain();
    }

    public static boolean isFirstMission() {
        return missionHistory.size() == 1;
    }
}
