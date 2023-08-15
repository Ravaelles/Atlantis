package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldFightInsteadAvoidAsRegularUnit extends Manager {
    public ShouldFightInsteadAvoidAsRegularUnit(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isHealthy()
            && unit.hasAnyWeapon()
            && unit.noCooldown()
            && unit.lastStartedRunningAgo() < unit.lastStartedAttackAgo();
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
