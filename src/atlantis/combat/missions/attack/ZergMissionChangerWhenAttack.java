package atlantis.combat.missions.attack;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.util.Enemy;

public class ZergMissionChangerWhenAttack extends MissionChangerWhenAttack {

    // === DEFEND ==============================================

    public boolean shouldChangeMissionToDefend() {
        if (defendAgainstMassZerglings()) {
            if (DEBUG) reason = "Mass zerglings G";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBase() && A.supplyUsed() <= 100) {
            if (DEBUG) reason = "Enemy near our building";
            return true;
        }

        if (ArmyStrength.weAreWeaker() && !Enemy.zerg()) {
            if (DEBUG) reason = "Hmm, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    // === CONTAIN =============================================

    public boolean shouldChangeMissionToContain() {
        if (A.supplyUsed() >= 170) return false;

        if (ArmyStrength.weAreWeaker()) {
            if (DEBUG) reason = "We are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (ArmyStrength.ourArmyRelativeStrength() <= 150 && A.seconds() <= 600) {
            if (DEBUG) reason = "Not strong enough (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (!GamePhase.isLateGame() && EnemyInfo.startedWithCombatBuilding && !ArmyStrength.weAreMuchStronger()) {
            if (DEBUG) reason = "startedWithCombatBuilding & !weAreMuchStronger";
            return true;
        }

        if (EnemyUnits.discovered().combatBuildings(false).atLeast(2)) {
            if (ArmyStrength.ourArmyRelativeStrength() <= 350) {
                if (DEBUG)
                    reason = "Caution with defensive buildings (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }
        }

        return false;

//        int ourCount = Select.ourCombatUnits().count();
//
//        return ourCount <= 10 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

}
