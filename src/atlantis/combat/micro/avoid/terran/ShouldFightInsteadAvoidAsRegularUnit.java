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
            && unit.isGroundUnit()
            && unit.hasAnyWeapon()
            && unit.noCooldown()
            && unit.lastStartedRunningAgo() < unit.lastAttackFrameAgo()
            && unit.combatEvalRelative() >= 0.6;
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
