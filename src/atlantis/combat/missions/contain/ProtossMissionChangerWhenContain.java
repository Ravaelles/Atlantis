package atlantis.combat.missions.contain;

import atlantis.combat.retreating.RetreatManager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;

public class ProtossMissionChangerWhenContain extends MissionChangerWhenContain {

    // === DEFEND ==============================================

    public boolean shouldChangeMissionToDefend() {
        if (ArmyStrength.ourArmyRelativeStrength() <= 199) {
            if (RetreatManager.GLOBAL_RETREAT_COUNTER >= 2 && A.resourcesBalance() <= 300) {
                if (DEBUG) reason = "We are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }

            if (GamePhase.isEarlyGame()) {
                if (DEBUG) reason = "Eh, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }
        }

        if (ArmyStrength.ourArmyRelativeStrength() <= 160) {
            if (DEBUG) reason = "Not so strong (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (defendAgainstMassZerglings()) {
            if (DEBUG) reason = "Mass zerglings";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBase() && A.supplyUsed() <= 70) {
            if (DEBUG) reason = "Enemy near our building";
            return true;
        }

        if (A.resourcesBalance() <= -400 && A.supplyUsed() <= 130 && !GamePhase.isLateGame()) {
            if (DEBUG) reason = "Too many resources lost";
            return true;
        }

        return false;
    }

    // === ATTACK ==============================================

    public boolean shouldChangeMissionToAttack() {
        if (A.supplyUsed() >= 190) {
            if (DEBUG) reason = "Supply blocked";
            return true;
        }

        Alpha alpha = Alpha.get();
        if (alpha.cohesionPercent() <= 70 || alpha.size() <= 15) return false;

        if (ArmyStrength.weAreMuchStronger() && !EnemyInfo.hasDefensiveLandBuilding(true)) {
            if (DEBUG) reason = "Much stronger";
            return true;
        }

        if (A.resourcesBalance() >= 410 && ArmyStrength.weAreStronger()) {
            if (DEBUG) reason = "Resources balance good";
            return true;
        }

        return false;
    }

}
