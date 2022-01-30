package atlantis.combat.missions.attack;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.select.Select;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (ArmyStrength.weAreStronger()) {
            return false;
        }

        return false;

//        int ourCount = Select.ourCombatUnits().count();
//
//        return ourCount <= 10 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

}
