package atlantis.combat.missions;

import atlantis.combat.missions.attack.MissionChangerWhenAttack;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.combat.missions.defend.MissionChangerWhenDefend;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

import java.util.ArrayList;

public class MissionChanger {

    public static final boolean DEBUG = true;
//    public static final boolean DEBUG = false;
    public static String debugReason = "";

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

        debugReason = "";

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
        if (A.isUms()) {
            return;
        }

        if (Missions.isFirstMission()) {
            if (Missions.isGlobalMissionAttack() && unit.friendsNear().atLeast(3)) {
                forceMissionContain();
            }
        }

//        if (!A.supplyUsed(180) && unit.friendsNear().atLeast(5)) {
//            forceMissionDefend();
//        }
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

//    public static void forceMissionDefend() {
//        Missions.setGlobalMissionDefend();
//    }

    protected static boolean defendAgainstMassZerglings() {
        if (Enemy.zerg() && A.seconds() <= 260 && EnemyUnits.visibleAndFogged().ofType(AUnitType.Zerg_Zergling).atLeast(9)) {
            if (DEBUG) debugReason = "Mass zerglings";
            return true;
        }

        return false;
    }

}
