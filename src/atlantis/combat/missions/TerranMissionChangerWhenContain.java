package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.units.Select;

public class TerranMissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToAttack()) {
            Missions.currentGlobalMission = Missions.ATTACK;
        }
    }

    // =========================================================

    private static boolean shouldChangeMissionToAttack() {
        if (killsBalanceSaysSo()) {
            return true;
        }

        if (Select.ourTanks().count() <= 4 && Select.ourCombatUnits().count() <= 30) {
            return false;
        }

        return true;
    }

    private static boolean killsBalanceSaysSo() {
        if (AGame.getTimeSeconds() <= 400 && AGame.killsLossesResourceBalance() >= 900) {
            return true;
        }

        if (AGame.getTimeSeconds() <= 700 && AGame.killsLossesResourceBalance() >= 1600) {
            return true;
        }

        return false;
    }

    /**
     * Defines how many military units we should have before pushing forward towards the enemy.
     */
//    private static int defineMinUnitsToStrategicallyAttack() {
//        return 18;
//    }
//
//    private static boolean shouldChangeMissionToContain() {
//        int ourCombatUnits = Select.ourCombatUnits().count();
//
//        return ourCombatUnits <= 13;
//    }

}
