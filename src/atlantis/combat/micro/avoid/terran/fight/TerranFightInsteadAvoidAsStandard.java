package atlantis.combat.micro.avoid.terran.fight;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class TerranFightInsteadAvoidAsStandard extends Manager {
    public TerranFightInsteadAvoidAsStandard(AUnit unit) {
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
        if (unit.isMarine() && Enemy.protoss() && unit.meleeEnemiesNearCount(1.9) >= 2) {
            return null;
        }

        return usedManager(this);
    }
}
