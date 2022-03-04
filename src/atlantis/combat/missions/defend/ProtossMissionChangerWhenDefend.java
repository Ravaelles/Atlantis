package atlantis.combat.missions.defend;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.combat.missions.contain.ProtossMissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.select.Select;

public class ProtossMissionChangerWhenDefend extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain() && !ProtossMissionChangerWhenContain.shouldChangeMissionToDefend()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (!ArmyStrength.weAreWeaker()) {
            if (DEBUG) debugReason = "We are stronger (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if ((GamePhase.isEarlyGame() && A.resourcesBalance() >= 300)) {
            if (DEBUG) debugReason = "resources balance is good";
            return true;
        }

        if (A.supplyUsed(90)) {
            if (DEBUG) debugReason = "Supply quite big";
            return true;
        }

        return false;

//        if (GamePhase.isEarlyGame()) {
//            return Select.ourCombatUnits().atLeast(13) || A.resourcesBalance() >= 350;
//        }
    }

}
