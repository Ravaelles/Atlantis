package atlantis.combat.missions;

import atlantis.units.Count;
import atlantis.units.Select;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        int ourCount = Select.ourCombatUnits().count();

        if (ourCount <= 10 || Select.enemyRealUnits().count() >= ourCount + 2) {
            return true;
        }

        return false;
    }

}
