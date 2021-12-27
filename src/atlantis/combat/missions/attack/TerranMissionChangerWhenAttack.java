package atlantis.combat.missions.attack;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.strategy.OurStrategy;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.A;

public class TerranMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
        else if (shouldChangeMissionToDefend()) {
            changeMissionTo(Missions.DEFEND);
        }
    }

    // === CONTAIN =============================================

    public static boolean shouldChangeMissionToContain() {
        int ourCount = Select.ourCombatUnits().count();

        if (OurStrategy.get().goingBio()) {
            return Count.ourCombatUnits() <= 33;
        }

        return ourCount <= 15 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

    public static boolean shouldChangeMissionToDefend() {
        if (A.supplyUsed(120)) {
            return false;
        }

        if (Have.base() && Select.enemyCombatUnits().inRadius(15, Select.main()).atLeast(5)) {
            return true;
        }

        return false;
    }

}
