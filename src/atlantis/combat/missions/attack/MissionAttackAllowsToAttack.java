package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.combat.advance.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class MissionAttackAllowsToAttack extends HasUnit {
    public MissionAttackAllowsToAttack(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
//        if (A.supplyUsed() <= 40) {
//            // Zealots vs Zealot fix
//            if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
//                return true;
//            }
//        }

        if (enemy.isABuilding()) {
            Manager manager = (new DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(unit)).invoke();

            if (manager != null) return false;
        }

        return true;
    }
}
