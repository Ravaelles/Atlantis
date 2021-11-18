package atlantis.combat.missions.attack;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.units.select.Select;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        int ourCount = Select.ourCombatUnits().count();

        return ourCount <= 10 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

}
