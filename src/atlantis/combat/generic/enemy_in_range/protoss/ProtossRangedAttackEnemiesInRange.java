package atlantis.combat.generic.enemy_in_range.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.information.enemy.EnemyInfo;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
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
        if (!unit.isRanged()) return false;
        if (unit.enemiesNear().empty()) return false;
        if (unit.cooldown() >= 9) return false;
        if (unit.lastAttackFrameLessThanAgo(45)) return false;
        if (unit.hp() <= 60 && unit.meleeEnemiesNearCount(3) > 0) return false;

        boolean noRangedEnemies = EnemyInfo.noRanged() || unit.enemiesNear().ranged().empty();
        if (!noRangedEnemies && unit.shieldDamageAtLeast(9)) return false;

        return (
            unit.lastAttackFrameMoreThanAgo(30 * 6)
                || (unit.shieldDamageAtMost(unit.combatEvalRelative() > 1.2 ? (Enemy.zerg() ? 18 : 68) : 8))
        )
//            && unit.lastAttackFrameMoreThanAgo(50)
//            && unit.lastUnderAttackMoreThanAgo(30 * 6)
            && (unit.shieldDamageAtMost(9) || unit.isSafeFromMelee())
            && (noRangedEnemies || unit.distToLeader() <= (8 + unit.woundPercent() / 25.0))
            && (noRangedEnemies || !unit.squadIsRetreating())
//            && unit.enemiesNear().ranged().canAttack(unit, 5).empty()
//            && unit.combatEvalRelative() >= 1
//            && (unit.shieldDamageAtMost(30) || unit.lastUnderAttackMoreThanAgo(30 * 5))
//            && unit.meleeEnemiesNearCount(2.2 + unit.woundPercent() / 80.0) == 0
//            && unit.lastAttackFrameMoreThanAgo(30 * 2)
            && unit.meleeEnemiesNearCount(2.8 + unit.woundPercent() / 90.0) <= 1;
    }

    private AUnit enemyInRangeToAttack() {
        Selection enemies = unit.enemiesNear().canBeAttackedBy(unit, 2);

        AUnit nearest = enemies.inShootRangeOf(unit).mostWounded();
        if (nearest != null) return nearest;

        return enemies.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if ((enemy = enemyInRangeToAttack()) == null) return null;

        if (enemy.isDead()) {
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
