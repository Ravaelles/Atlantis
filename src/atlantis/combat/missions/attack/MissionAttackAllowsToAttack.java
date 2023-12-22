package atlantis.combat.missions.attack;

import atlantis.architecture.Manager;
import atlantis.combat.advance.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.game.A;
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

        if (forbiddenToAttackCombatBuilding(enemy)) return false;
        if (forbiddenToAttackWithinChoke(enemy)) return false;

        return true;
    }

    private boolean forbiddenToAttackWithinChoke(AUnit enemy) {
        if (unit.isAir() || unit.isMelee()) return false;
        if (enemy.isABuilding()) return false;

        if (A.supplyUsed() >= 190 || A.hasMinerals(2500)) return false;

        return unit.isWithinChoke();
    }

    private boolean forbiddenToAttackCombatBuilding(AUnit enemy) {
        if (enemy.isABuilding()) {
            Manager manager = (new DontAdvanceButHoldAndContainWhenEnemyBuildingsClose(unit)).invoke(this);

            if (manager != null) return true;
        }
        return false;
    }
}
