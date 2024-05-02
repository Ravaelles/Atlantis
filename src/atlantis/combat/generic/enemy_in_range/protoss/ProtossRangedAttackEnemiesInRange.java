package atlantis.combat.generic.enemy_in_range.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ProtossRangedAttackEnemiesInRange extends Manager {
    private AUnit enemy;

    public ProtossRangedAttackEnemiesInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isRanged()
            && unit.enemiesNear().notEmpty()
            && unit.combatEvalRelative() >= 1
            && unit.meleeEnemiesNearCount(2.8) == 0
            && unit.lastAttackFrameMoreThanAgo(30 * 6)
            && (unit.shieldDamageAtMost(30) || unit.lastUnderAttackMoreThanAgo(30 * 6))
            && (enemy = enemyInRangeToAttack()) != null;
    }

    private AUnit enemyInRangeToAttack() {
        return unit.enemiesNear().inShootRangeOf(unit).mostWounded();
    }

    @Override
    public Manager handle() {
        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemy)) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - OK - RangedAttackEnemy");

            unit.addLog("RangedAttackEnemy");
            return usedManager(this);
        }

        return null;
    }
}
