package atlantis.combat.missions;

import atlantis.AGame;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TerranMissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            Missions.setGlobalMissionDefend();
        }

        else if (shouldChangeMissionToAttack()) {
            Missions.setGlobalMissionAttack();
        }
    }

    // =========================================================

    private static boolean shouldChangeMissionToDefend() {
        return Select.ourTanks().count() == 0 || Select.ourCombatUnits().count() <= 10;
    }

    private static boolean shouldChangeMissionToAttack() {
        if (killsBalanceSaysSo()) {
            return true;
        }

        return Select.ourTanks().count() >= 2 || Select.ourCombatUnits().count() >= 25;
    }

    private static boolean killsBalanceSaysSo() {
        if (AGame.timeSeconds() <= 400 && AGame.killsLossesResourceBalance() >= 900) {
            return true;
        }

        return AGame.timeSeconds() <= 700 && AGame.killsLossesResourceBalance() >= 1600;
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
