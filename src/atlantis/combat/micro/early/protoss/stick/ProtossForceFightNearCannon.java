package atlantis.combat.micro.early.protoss.stick;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class ProtossForceFightNearCannon extends Manager {
    private AUnit cannon;
    private AUnit enemy;

    public ProtossForceFightNearCannon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (asBadlyWoundedDoNotFight()) return false;
        if (unit.cooldown() >= 8) return false;

        cannon = unit.friendsNear().cannons().inRadius(3.5, unit).nearestTo(unit);
        if (cannon == null) return false;

        enemy = cannon.enemiesNear().inRadius(6.2, cannon).notDeadMan().nearestTo(unit);
        if (cannon == null) return false;

        return enemy != null;
    }

    private boolean asBadlyWoundedDoNotFight() {
        if (unit.isMelee()) return unit.hp() <= 50 && unit.hasCooldown();

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
