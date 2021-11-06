package atlantis.combat.missions;

import atlantis.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TerranMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        int ourCount = Select.ourCombatUnits().count();

        if (OurStrategy.get().goingBio()) {
            return Count.ourCombatUnits() <= 12;
        }

        return ourCount <= 15 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

}
