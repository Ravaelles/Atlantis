package atlantis.combat.generic.enemy_in_range.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class ProtossRangedAttackEnemiesInRange extends Manager {
    private AUnit enemy;

    public ProtossRangedAttackEnemiesInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (!We.protoss()) return false;

        boolean noRanged = EnemyInfo.noRanged();
        if (!noRanged && unit.shieldDamageAtLeast(6)) return false;

        return unit.isRanged()
            && unit.shieldDamageAtMost(30)
//            && unit.lastAttackFrameMoreThanAgo(50)
            && (unit.isHealthy() || unit.isSafeFromMelee())
            && (noRanged || unit.distToLeader() <= (5 + unit.woundPercent() / 10.0))
            && unit.cooldown() <= 5
            && unit.enemiesNear().notEmpty()
            && (noRanged || !unit.squadIsRetreating())
//            && unit.enemiesNear().ranged().canAttack(unit, 5).empty()
//            && unit.combatEvalRelative() >= 1
            && (unit.shieldDamageAtMost(30) || unit.lastUnderAttackMoreThanAgo(30 * 5))
//            && unit.meleeEnemiesNearCount(2.2 + unit.woundPercent() / 80.0) == 0
//            && unit.lastAttackFrameMoreThanAgo(30 * 2)
            && (enemy = enemyInRangeToAttack()) != null;
    }

    private AUnit enemyInRangeToAttack() {
        Selection enemies = unit.enemiesNear().canBeAttackedBy(unit, 2);

        AUnit nearest = enemies.inShootRangeOf(unit).mostWounded();
        if (nearest != null) return nearest;

        return enemies.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if (unit.isDead()) {
            ErrorLog.printMaxOncePerMinute("Dead target inRange (" + enemy + ") for " + unit);
            return null;
        }

        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemy)) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - OK - RangedAttackEnemy");

            unit.addLog("RangedAttackEnemy");
            return usedManager(this);
        }

        return null;
    }
}
