package atlantis.combat.micro.avoid.terran.fight;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranFightInsteadAvoidAsFirebat extends Manager {
    public TerranFightInsteadAvoidAsFirebat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isFirebat()) return false;

        if (shouldProtectNearbyTanks()) return true;

        return unit.hp() >= 40
            && (unit.friendsInRadiusCount(3) >= 4 || longNotUnderAttackAndCloseToEnemy());
    }

    private boolean shouldProtectNearbyTanks() {
        return unit.cooldownRemaining() <= 3
            && unit.hp() >= 21
            && unit.friendsNear().tanks().inRadius(5, unit).notEmpty();
    }

    private boolean longNotUnderAttackAndCloseToEnemy() {
        return unit.hp() >= 45
            && (unit.isHealthy() || unit.isMissionDefend())
            && unit.lastUnderAttackMoreThanAgo(30 * 3) && unit.enemiesNear().inRadius(3, unit).notEmpty();
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
