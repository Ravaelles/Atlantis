package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.util.Enemy;
import atlantis.util.log.ErrorLog;

public class ReaverUseTransport extends Manager {

    private AUnit shuttle;

    public ReaverUseTransport(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (unit.scarabCount() == 0 && unit.enemiesNear().canAttack(unit, 2).notEmpty()) return true;
        if (unit.lastActionLessThanAgo(6, Actions.LOAD)) return true;

        if (unit.isAttacking()) return false;
        if (unit.isAttackFrame()) return false;
        if (unit.isStartingAttack()) return false;
        if (unit.lastActionLessThanAgo(35, Actions.UNLOAD)) return false;
        if (unit.lastActionLessThanAgo(60, Actions.LOAD)) return false;

        if (safeAgainstEnemiesAndHasTargets()) return false;

//        System.err.println(unit.lastActionAgo(Actions.UNLOAD));
        if (unit.hp() >= 80 && unit.lastActionLessThanAgo(15, Actions.ATTACK_UNIT)) return false;

        if (
//            unit.shotSecondsAgo() > 4
            unit.enemiesNearInRadius(7.5) == 0
                && unit.enemiesNearInRadius(10) > 0
        ) return false;

        shuttle = unit.friendsNear()
            .ofType(AUnitType.Protoss_Shuttle)
            .havingAtLeastHp(30)
            .havingSpaceFree(2)
            .nearestTo(unit);

        return shuttle != null;
    }

    private boolean safeAgainstEnemiesAndHasTargets() {
        return !Enemy.terran()
            && unit.enemiesNear().groundUnits().canAttack(unit, unit.shields() >= 40 ? 0.9 : 1.9).empty()
            && unit.enemiesNear().groundUnits().canBeAttackedBy(unit, 0.5).notEmpty();
    }

    @Override
    public Manager handle() {
        if (shuttle == null) {
//            ErrorLog.printMaxOncePerMinute("ReaverUseTransport: shuttle is null");
            return null;
        }
        if (!shuttle.hasFreeSpaceFor(unit)) {
            ErrorLog.printMaxOncePerMinute("ReaverUseTransport: no space for reaver");
            return null;
        }

        if (unit.load(shuttle)) {
            shuttle.load(unit);
            shuttle.setTooltip("Load reaver");
            return usedManager(this);
        }

        return null;
    }
}
