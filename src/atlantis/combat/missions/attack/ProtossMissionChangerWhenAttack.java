package atlantis.combat.missions.attack;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenAttack {

    // === DEFEND ==============================================
    public boolean shouldChangeMissionToDefend() {
        if (defendAgainstMassZerglings()) {
            if (DEBUG) reason = "Mass zerglings";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBase() && A.supplyUsed() <= 70) {
            if (DEBUG) reason = "Enemy is near our building";
            return true;
        }

        if (ArmyStrength.weAreWeaker()) {
            if (DEBUG) reason = "Hmm, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (enemyHasHiddenUnitsAndWeDontHaveEnoughDetection()) {
            if (DEBUG) reason = "Not enough detection";
            return true;
        }

        return false;
    }

    // =========================================================

    private boolean enemyHasHiddenUnitsAndWeDontHaveEnoughDetection() {
        if (Count.observers() > 0) return false;

        return EnemyUnits.discovered().effUndetected().size() >= 2;
    }

    // === CONTAIN =============================================

    public boolean shouldChangeMissionToContain() {
        if (true) return false;
        if (A.supplyUsed() >= 176) return false;

        if (ArmyStrength.ourArmyRelativeStrength() <= 270) {
            if (DEBUG) reason = "Not strong enough to attack (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (!GamePhase.isLateGame() && EnemyInfo.startedWithCombatBuilding && !ArmyStrength.weAreMuchStronger()) {
            if (DEBUG) reason = "startedWithCombatBuilding & !weAreMuchStronger";
            return true;
        }

        return false;
    }

}
