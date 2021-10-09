package atlantis.combat.missions;

import atlantis.units.Count;
import atlantis.units.Select;

public class ProtossMissionChangerWhenContain extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            changeMissionTo(Missions.DEFEND);
        } else if (shouldChangeMissionToAttack()) {
            changeMissionTo(Missions.ATTACK);
        }
    }

    // === ATTACK ==============================================

    private static boolean shouldChangeMissionToAttack() {
        int ourCombatUnits = Count.countOurCombatUnits();

        if (ourCombatUnits >= 18) {
            return true;
        }

        return false;
    }

    // === DEFEND ==============================================

    private static boolean shouldChangeMissionToDefend() {
        int ourCombatUnits = Count.countOurCombatUnits();

        return ourCombatUnits <= 6;
    }

}
