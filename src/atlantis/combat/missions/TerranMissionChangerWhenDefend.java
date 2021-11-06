package atlantis.combat.missions;

import atlantis.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.A;

public class TerranMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (isFirstMission()) {
            if (OurStrategy.get().isRush()) {
                return Count.ourCombatUnits() >= 12 || A.resourcesBalance() >= 350;
            }

            return Count.ourCombatUnits() >= 5;
        }

//        return Count.ourCombatUnits() >= Math.max(24, 12 + Missions.counter());
        return Count.ourCombatUnits() >= 15;
    }

}
