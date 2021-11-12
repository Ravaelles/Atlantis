package atlantis.combat.missions;

import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.A;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        return Select.ourCombatUnits().atLeast(12) || A.resourcesBalance() >= 450;
    }

}
