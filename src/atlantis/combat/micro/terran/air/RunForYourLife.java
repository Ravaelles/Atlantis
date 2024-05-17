package atlantis.combat.micro.terran.air;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.units.AUnit;

public class RunForYourLife extends Manager {
    public RunForYourLife(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAir()) return false;

        if (
            unit.lastUnderAttackLessThanAgo(30 * 3)
                && unit.enemiesNear().combatBuildingsAntiAir().inRadius(11, unit).notEmpty()
        ) return true;

        if (unit.hasCloseRepairer()) return false;
        if (unit.effUndetected()) return false;

        return unit.hp() <= 65
            && unit.enemiesNear().canAttack(unit, 8.1).notEmpty();
    }

    @Override
    protected Manager handle() {
        if (unit.isRunning() && unit.lastStartedRunningLessThanAgo(16)) return usedManager(this);

        if ((new AvoidEnemies(unit)).invokeFrom(this) != null) return usedManager(this);

        return null;
    }
}
