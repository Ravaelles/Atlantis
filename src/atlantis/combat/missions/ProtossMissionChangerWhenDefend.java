package atlantis.combat.missions;

import atlantis.units.select.Select;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        return Select.ourCombatUnits().atLeast(10);
    }

}
