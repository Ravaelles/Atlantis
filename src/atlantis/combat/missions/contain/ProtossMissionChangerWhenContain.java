package atlantis.combat.missions.contain;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.retreating.RetreatManager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.select.Count;

public class ProtossMissionChangerWhenContain extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            MissionChanger.changeMissionTo(Missions.DEFEND);
        } else if (shouldChangeMissionToAttack()) {
            MissionChanger.changeMissionTo(Missions.ATTACK);
        }
    }

    // === DEFEND ==============================================

    public static boolean shouldChangeMissionToDefend() {
        if (ArmyStrength.weAreWeaker()) {
            if (RetreatManager.GLOBAL_RETREAT_COUNTER >= 2 && A.resourcesBalance() <= 300) {
                if (DEBUG) debugReason = "We are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }

            if (GamePhase.isEarlyGame()) {
                if (DEBUG) debugReason = "Eh, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }
        }

        if (defendAgainstMassZerglings()) {
            if (DEBUG) debugReason = "Mass zerglings";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBuilding() && A.supplyUsed() <= 70) {
            if (DEBUG) debugReason = "Enemy near our building";
            return true;
        }

        if (A.resourcesBalance() <= -400 && A.supplyUsed() <= 130 && !GamePhase.isLateGame()) {
            if (DEBUG) debugReason = "Too many resources lost";
            return true;
        }

        return false;
    }

    // === ATTACK ==============================================

    private static boolean shouldChangeMissionToAttack() {
        if (A.supplyUsed() >= 194) {
            if (DEBUG) debugReason = "Supply blocked";
            return true;
        }

        if (ArmyStrength.weAreMuchStronger() && !EnemyInfo.hasDefensiveLandBuilding(true)) {
            if (DEBUG) debugReason = "Much stronger";
            return true;
        }

        if (A.resourcesBalance() >= 410 && ArmyStrength.weAreStronger()) {
            if (DEBUG) debugReason = "Resources balance good";
            return true;
        }

        return false;
    }

}
