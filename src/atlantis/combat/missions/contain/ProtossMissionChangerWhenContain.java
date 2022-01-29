package atlantis.combat.missions.contain;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.enemy.EnemyInformation;
import atlantis.units.select.Count;
import atlantis.util.A;

public class ProtossMissionChangerWhenContain extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            MissionChanger.changeMissionTo(Missions.DEFEND);
        } else if (shouldChangeMissionToAttack()) {
            MissionChanger.changeMissionTo(Missions.ATTACK);
        }
    }

    // === DEFEND ==============================================

    private static boolean shouldChangeMissionToDefend() {
        if (EnemyInformation.isEnemyNearAnyOurBuilding()) {
            return true;
        }

        if (Missions.isFirstMission()) {
            return false;
        }

        int ourCombatUnits = Count.ourCombatUnits();

        return ourCombatUnits <= 6;
    }

    // === ATTACK ==============================================

    private static boolean shouldChangeMissionToAttack() {
//        if (AGame.killsLossesResourceBalance() <= 100) {
//            return false;
//        }

        if (A.supplyUsed() >= 190 || Count.ourCombatUnits() >= 35) {
            return true;
        }

        return false;
//        if (AGame.timeSeconds() <= 400 && AGame.killsLossesResourceBalance() >= 900) {
//            return true;
//        }
//
//        return AGame.timeSeconds() <= 700 && AGame.killsLossesResourceBalance() >= 1600;
    }

}
