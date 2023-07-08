package atlantis.combat.missions.attack;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.contain.MissionChangerWhenContain;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.units.select.Select;

public class ZergMissionChangerWhenAttack extends MissionChangerWhenContain {

    public void changeMissionIfNeeded() {
        if (shouldChangeMissionToDefend()) {
            changeMissionTo(MissionChanger.defendOrSpartaMission());
        }
//        else if (shouldChangeMissionToContain()) {
//            changeMissionTo(Missions.CONTAIN);
//        }
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
        if (A.supplyUsed() >= 170) {
            return false;
        }

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
                if (DEBUG) reason = "Caution with defensive buildings (" + ArmyStrength.ourArmyRelativeStrength() + "%)";
                return true;
            }
        }

        return false;

//        int ourCount = Select.ourCombatUnits().count();
//
//        return ourCount <= 10 || Select.enemyRealUnits().count() >= ourCount + 2;
    }

}
