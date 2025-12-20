package atlantis.combat.micro.early.protoss.stick;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.game.player.Enemy;

public class ProtossForceFightNearCannon extends Manager {
    private AUnit cannon;
    private AUnit enemy;

    public ProtossForceFightNearCannon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isAir()) return false;
        if (unit.cooldown() >= 9) return false;
        if (unit.cooldown() >= 7 && unit.hp() <= 80) return false;

        cannon = unit.friendsNear().cannons().inRadius(3.5, unit).nearestTo(unit);
        if (cannon == null) return false;

        enemy = enemyToAttack();
        if (enemy == null) return false;

        double enemyToCannon = enemy.distTo(cannon);
        if (enemyToCannon - enemy.groundWeaponRange() >= 0.2) return false;

        if (unit.isReaver() && (unit.hp() <= 120 || unit.cooldown() >= 3)) return false;
        if (asBadlyWoundedDoNotFight()) return false;
        if (unit.cooldown() >= (unit.hp() <= 60 ? 3 : 8)) return false;
        if (Enemy.protoss()) {
            if (unit.cooldown() >= 1 && unit.shieldWound() >= 40 && unit.lastUnderAttackLessThanAgo(50)) return false;
        }

        if (enemyToCannon >= 5.9 && (unit.hp() <= 40 || unit.lastUnderAttackLessThanAgo(50))) return false;

        return true;
    }

    private AUnit enemyToAttack() {
        return cannon.enemiesNear().inRadius(6.7, cannon)
            .notDeadMan()
            .effVisible()
            .canBeAttackedBy(unit, unit.isMelee() ? 4 : 1)
            .nearestTo(unit);
    }

    private boolean asBadlyWoundedDoNotFight() {
        if (unit.isMelee()) return unit.hp() <= 60 && unit.cooldown() >= 6;

        return unit.hp() <= (Enemy.protoss() ? 40 : 30)
            && unit.cooldown() >= 8
            && unit.meleeEnemiesNearCount(2.3) > 0;
    }

    @Override
    public Manager handle() {
        if (unit.attackUnit(enemy)) {
            return usedManager(this);
        }

        return null;
    }
}
