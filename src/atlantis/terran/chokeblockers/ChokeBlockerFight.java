package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class ChokeBlockerFight extends Manager {
    public ChokeBlockerFight(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.hp() <= 24) return false;

        if (unit.isZealot()) {
            if (appliesAsWoundedZealot()) return false;
            if (anyOtherBlockerIsFighting()) return true;

            return false;
        }

        return !unit.isScv()
            && unit.hp() >= 40;
//            && unit.lastUnderAttackLessThanAgo(40);
    }

    private boolean appliesAsWoundedZealot() {
        if (unit.cooldown() >= 5) return false;

        if (unit.hp() <= 46) {
            if (
                unit.hp() <= 36
//                    && Count.dragoons() <= 2
                    && unit.lastAttackFrameLessThanAgo(30 * 5)
            ) return false;
        }
        return true;
    }

    @Override
    public Manager handle() {
        AUnit enemyInRange = enemyInRange();
        if (enemyInRange != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(enemyInRange)) return usedManager(this);
        }

        AUnit nearestEnemy = possibleEnemies().inRadius(2.5, unit).nearestTo(unit);
        if (nearestEnemy != null) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(nearestEnemy)) return usedManager(this);
        }

        AUnit breachedBase = EnemyWhoBreachedBase.get();
        if (breachedBase != null && breachedBase.isDetected() && unit.canAttackTarget(breachedBase)) {
            if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(breachedBase)) return usedManager(this);
        }

        return null;
    }

    private Selection possibleEnemies() {
        return unit.enemiesNear().groundUnits().effVisible();
    }

    private boolean anyOtherBlockerIsFighting() {
        for (AUnit blocker : ChokeBlockersAssignments.get().blockers) {
            if (unit.equals(blocker)) continue;

            if (blocker.isAttacking()) return true;
            if (blocker.lastUnderAttackLessThanAgo(10)) return true;
        }

        return false;
    }

    private AUnit enemyInRange() {
        return possibleEnemies().canBeAttackedBy(unit, maxDistToAttack() - 1).mostWounded();
    }

    private double maxDistToAttack() {
        int maxEnemies = Enemy.protoss() ? 1 : 3;

        if (
            possibleEnemies().inRadius(7, unit).groundUnits().count() <= maxEnemies
        ) return 2.5;

        return 1.4;
    }
}
