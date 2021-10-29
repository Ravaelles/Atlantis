package atlantis.combat.missions;

import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ZergMissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToAttack()) {
            Missions.currentGlobalMission = Missions.ATTACK;
        }
    }

    // =========================================================

    private static boolean shouldChangeMissionToAttack() {
        return true;
    }


    /**
     * Defines how many military units we should have before pushing forward towards the enemy.
     */
    private static int defineMinUnitsToStrategicallyAttack() {
        return 15;
    }

    private static boolean shouldChangeMissionToContain() {
        int ourCombatUnits = Select.ourCombatUnits().count();

        return ourCombatUnits <= 13;
    }

}
