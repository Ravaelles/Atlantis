package atlantis.combat.running.stop_running.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;

public class TerranShouldStopRetreat extends Manager {
    public TerranShouldStopRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isRetreating()) return false;

        if (!unit.isMoving()) return true;

        if (unit.lastStartedRunningLessThanAgo(20)) return false;

        return unit.lastUnderAttackMoreThanAgo(70)
            && unit.enemiesNear().canAttack(unit, 2.3).isEmpty();

//        return unit.lastStartedRunningMoreThanAgo(30 * 3)
//            || unit.combatEvalRelative() >= 1.3
//            || (unit.cooldown() <= 7 && (unit.distToCannon() <= 1.9 || unit.distToBase() <= 5));
    }

    @Override
    public Manager handle() {
        unit.runningManager().stopRunning();

        if ((new AttackNearbyEnemies(unit)).invokeFrom(this) != null) {
            return usedManager(this);
        }

        return null;
    }
}
