package atlantis.combat.missions;

import atlantis.units.Select;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
//        else if (shouldChangeMissionToAttack()) {
//            changeMissionTo(Missions.ATTACK);
//        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (Select.ourCombatUnits().count() >= 10) {
            return false;
        }

        return false;
    }

    // === ATTACK ==============================================

//    private static boolean shouldChangeMissionToAttack() {
//        if (Select.ourCombatUnits().count() >= 18) {
//            return false;
//        }
//
//        return false;
//    }

    // =========================================================


}
