package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenContain {

    public void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            changeMissionTo(MissionChanger.defendOrSpartaMission());
        }
        else if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
    }

    // === DEFEND ==============================================

    public boolean shouldChangeMissionToDefend() {
        if (defendAgainstMassZerglings()) {
            if (DEBUG) reason = "Mass zerglings";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBase() && A.supplyUsed() <= 100) {
            if (DEBUG) reason = "Enemy near our building";
            return true;
        }

        if (ArmyStrength.weAreWeaker()) {
            if (DEBUG) reason = "Hmm, we are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        return false;
    }

    // === CONTAIN =============================================

    private boolean shouldChangeMissionToContain() {
        if (A.supplyUsed() >= 176) {
            return false;
        }

        if (ArmyStrength.ourArmyRelativeStrength() <= 270) {
            if (DEBUG) reason = "Not strong enough to attack (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (!GamePhase.isLateGame() && EnemyInfo.startedWithCombatBuilding && !ArmyStrength.weAreMuchStronger()) {
            if (DEBUG) reason = "startedWithCombatBuilding & !weAreMuchStronger";
            return true;
        }

        return false;

//        int ourCount = Select.ourCombatUnits().count();
//
//        return ourCount <= 10 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

}
