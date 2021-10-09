package atlantis.combat.missions;

import atlantis.units.Count;
import atlantis.units.Select;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
//        if (shouldChangeMissionToDefend()) {
//            changeMissionTo(Missions.DEFEND);
//        }
//        else
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (Select.ourCombatUnits().count() <= 10) {
            return false;
        }

        return false;
    }

    // === DEFEND ==============================================

//    private static boolean shouldChangeMissionToDefend() {
//        if (Select.ourCombatUnits().count() <= 5) {
//            return false;
//        }
//
//        return false;
//    }

    // =========================================================

}
