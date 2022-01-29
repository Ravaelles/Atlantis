package atlantis.combat.missions.defend;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.game.A;
import atlantis.units.select.Select;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        return Select.ourCombatUnits().atLeast(13) || A.resourcesBalance() >= 450;
    }

}
