package atlantis.combat.missions.defend;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.select.Select;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (ArmyStrength.weAreStronger()) {
            if (DEBUG) debugReason = "weAreStronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (A.resourcesBalance() >= 300) {
            if (DEBUG) debugReason = "resourcesBalance good";
            return true;
        }

        return false;

//        if (GamePhase.isEarlyGame()) {
//            return Select.ourCombatUnits().atLeast(13) || A.resourcesBalance() >= 350;
//        }
    }

}
