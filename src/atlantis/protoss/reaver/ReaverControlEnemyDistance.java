package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ReaverControlEnemyDistance extends Manager {
    public ReaverControlEnemyDistance(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.shieldWounded()) return false;
        if (unit.enemiesNear().nonBuildings().canAttack(unit, 1.2).empty()) return false;

        if (unit.isStartingAttack()) return false;
        if (unit.isAttackFrame()) return false;
        if (unit.shotSecondsAgo() > 2.6) return false;
        if (unit.lastActionLessThanAgo(15, Actions.ATTACK_UNIT)) return false;

//        System.out.println(unit.lastActionAgo(Actions.UNLOAD));

        return (unit.cooldown() >= 6 && unit.lastActionMoreThanAgo(40, Actions.UNLOAD))
            || unit.lastUnderAttackLessThanAgo(90);
    }

    @Override
    public Manager handle() {
        AUnit nearestEnemy = unit.enemiesNear().groundUnits().havingWeapon().nearestTo(unit);
        if (nearestEnemy == null) return null;

        if (unit.runningManager().runFrom(nearestEnemy, 1.5, Actions.RUN_ENEMY, true)) {
            return usedManager(this);
        }

        return null;
    }
}
