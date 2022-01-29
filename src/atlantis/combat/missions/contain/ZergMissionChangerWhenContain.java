package atlantis.combat.missions.contain;

import atlantis.combat.missions.Missions;
import atlantis.units.select.Select;

public class ZergMissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToAttack()) {
            Missions.setGlobalMissionAttack();
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
