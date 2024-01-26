package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

public class ChokeBlockerFight extends Manager {
    public ChokeBlockerFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.hp() <= 24) return false;

        if (unit.isZealot()) {
            if (unit.hp() <= 46) {
                if (unit.hasCooldown()) return false;
                if (unit.hp() <= 34 && unit.lastAttackFrameLessThanAgo(30 * 9)) return false;
            }
//            if (anyOtherBlockerIsFighting()) return true;

            return true;
        }

        return !unit.isScv()
            && unit.hp() >= 40;
//            && unit.lastUnderAttackLessThanAgo(40);
    }

    @Override
    public Manager handle() {
        AUnit enemyInRange = enemyInRange();
        if (enemyInRange != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);
        }

        AUnit breachedBase = EnemyWhoBreachedBase.get();
        if (breachedBase != null && breachedBase.isDetected() && unit.canAttackTarget(breachedBase)) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(breachedBase)) return usedManager(this);
        }

        return null;
    }

    private boolean anyOtherBlockerIsFighting() {
        for (AUnit blocker : ChokeBlockersAssignments.get().blockers) {
            if (unit.equals(blocker)) continue;

            if (blocker.isAttacking()) return true;
        }

        return false;
    }

    private AUnit enemyInRange() {
        return unit.enemiesNear().groundUnits().canBeAttackedBy(unit, maxDistToAttack() - 1).mostWounded();
    }

    private double maxDistToAttack() {
        int maxEnemies = Enemy.protoss() ? 1 : 3;

        if (
            unit.enemiesNear().inRadius(7, unit).groundUnits().count() <= maxEnemies
        ) return 2.5;

        return 1.4;
    }
}
