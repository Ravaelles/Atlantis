package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldFightInsteadAvoidAsRegularUnit extends Manager {
    public ShouldFightInsteadAvoidAsRegularUnit(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        System.err.println("unit.combatEvalRelative() = " + unit.combatEvalRelative());
        return unit.isHealthy()
            && unit.hasAnyWeapon()
            && unit.noCooldown()
            && unit.lastStartedRunningAgo() < unit.lastStartedAttackAgo()
            && unit.combatEvalRelative() >= 0.6;
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
