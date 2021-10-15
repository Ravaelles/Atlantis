package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.Select;

import java.util.ArrayList;

public class MissionChanger {

    protected static ArrayList<Mission> missionHistory = new ArrayList<>();

    // =========================================================

    /**
     * Takes care of current strategy.
     */
    public static void evaluateGlobalMission() {
        if (Missions.currentGlobalMission == null) {
            Missions.currentGlobalMission = Missions.getInitialMission();
        }

        // === Handle UMS ==========================================

        if (AGame.isUms() || Select.mainBase() == null) {
            return;
        }

        // =========================================================

        if (Missions.currentGlobalMission == Missions.ATTACK) {
            MissionChangerWhenAttack.changeMissionIfNeeded();
        } else if (Missions.currentGlobalMission == Missions.CONTAIN) {
            MissionChangerWhenContain.changeMissionIfNeeded();
        } else if (Missions.currentGlobalMission == Missions.DEFEND) {
            MissionChangerWhenDefend.changeMissionIfNeeded();
        }

//        if (Missions.currentGlobalMission != Missions.ATTACK) {
//        } else if (shouldChangeMissionToContain()) {
//            Missions.currentGlobalMission = Missions.CONTAIN;
//        }
    }


    public static void notifyThatUnitRetreated(AUnit unit) {
        if (isFirstMission() && Missions.isGlobalMissionAttack()) {
            forceMissionContain();
        }
    }

    // =========================================================

    protected static void changeMissionTo(Mission newMission) {
        Missions.currentGlobalMission = newMission;
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
        Missions.currentGlobalMission = Missions.ATTACK;
    }

    public static void forceMissionContain() {
        Missions.currentGlobalMission = Missions.CONTAIN;
    }

    public static boolean isFirstMission() {
        return missionHistory.size() == 1;
    }
}
