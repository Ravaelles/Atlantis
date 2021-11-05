package atlantis.combat.missions;

import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TerranMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (isFirstMission() && Select.ourCombatUnits().atLeast(3)) {
            return true;
        }

        return Select.ourCombatUnits().atLeast(13);
    }

}
