package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.combat.missions.attack.MissionChangerWhenAttack;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.combat.missions.defend.MissionChangerWhenDefend;
import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.select.Have;

import java.util.ArrayList;

public class MissionChanger {

    protected static ArrayList<Mission> missionHistory = new ArrayList<>();

    // =========================================================

    /**
     * Takes care of current strategy.
     */
    public static void evaluateGlobalMission() {

        // === Handle UMS ==========================================

        if (AGame.isUms()) {
            forceMissionAttack();
            return;
        }

        // =========================================================

        if (!Have.main()) {
            return;
        }

        if (Missions.isGlobalMissionAttack()) {
            MissionChangerWhenAttack.changeMissionIfNeeded();
        } else if (Missions.isGlobalMissionContain()) {
            MissionChangerWhenContain.changeMissionIfNeeded();
        } else if (Missions.isGlobalMissionDefend()) {
            MissionChangerWhenDefend.changeMissionIfNeeded();
        }
    }

    // =========================================================

    public static void notifyThatUnitRetreated(AUnit unit) {
        if (
                Missions.isFirstMission()
                    && Missions.isGlobalMissionAttack()
                    && RetreatManager.GLOBAL_RETREAT_COUNTER >= 4
        ) {
            forceMissionContain();
        }
    }

    // =========================================================

    protected static void changeMissionTo(Mission newMission) {
        Missions.setGlobalMissionTo(newMission);
        missionHistory.add(newMission);
    }

    public static void forceMissionAttack() {
        Missions.setGlobalMissionAttack();
    }

    public static void forceMissionContain() {
        Missions.setGlobalMissionContain();
    }

}
