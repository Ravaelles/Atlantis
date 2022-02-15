package atlantis.combat.missions.attack;

import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.combat.retreating.RetreatManager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnitType;
import atlantis.util.Enemy;

public class ProtossMissionChangerWhenAttack extends MissionChangerWhenContain {

    public static void changeMissionIfNeeded() {
        if (shouldChangeMissionToContain()) {
            changeMissionTo(Missions.CONTAIN);
        }
        else if (shouldChangeMissionToDefend()) {
            changeMissionTo(Missions.DEFEND);
        }
    }

    // === DEFEND ==============================================

    private static boolean shouldChangeMissionToDefend() {
        if (defendAgainstMassZerglings()) {
            if (DEBUG) debugReason = "Mass zerglings";
            return true;
        }

        if (EnemyInfo.isEnemyNearAnyOurBuilding() && A.supplyUsed() <= 70) {
            if (DEBUG) debugReason = "Enemy near our building";
            return true;
        }

        return false;
    }

    // === CONTAIN =============================================

    private static boolean shouldChangeMissionToContain() {
        if (ArmyStrength.weAreWeaker()) {
            if (DEBUG) debugReason = "We are weaker (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
            return true;
        }

        if (!GamePhase.isLateGame() && EnemyInfo.startedWithCombatBuilding && !ArmyStrength.weAreMuchStronger()) {
            if (DEBUG) debugReason = "startedWithCombatBuilding & !weAreMuchStronger";
            return true;
        }

        return false;

//        int ourCount = Select.ourCombatUnits().count();
//
//        return ourCount <= 10 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

}
