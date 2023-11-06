package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranFightInsteadAvoidAsFirebat extends Manager {
    public TerranFightInsteadAvoidAsFirebat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isFirebat() && unit.hp() >= 40 && (unit.friendsInRadiusCount(3) >= 4 || longNotUnderAttackAndCloseToEnemy());
    }

    private boolean longNotUnderAttackAndCloseToEnemy() {
        return unit.hp() >= 45
            && unit.isMissionDefend()
            && unit.lastUnderAttackMoreThanAgo(30 * 3) && unit.enemiesNear().inRadius(3, unit).notEmpty();
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
