package atlantis.combat.missions;

import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ZergMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        int ourCount = Select.ourCombatUnits().count();

        return ourCount <= 20 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

}
