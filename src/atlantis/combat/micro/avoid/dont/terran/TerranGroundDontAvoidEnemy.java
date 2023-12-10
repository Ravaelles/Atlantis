package atlantis.combat.micro.avoid.dont.terran;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;

public class TerranGroundDontAvoidEnemy extends Manager {
    public TerranGroundDontAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isGroundUnit()) return false;
        if (!unit.isRunning()) return false;

        if (
            unit.noCooldown()
                && unit.woundPercent() <= 40
                && unit.lastAttackFrameMoreThanAgo(30 * 7)
                && unit.lastStartedRunningMoreThanAgo(30 * 3)
        ) return true;

        return false;
    }

    @Override
    public Manager handle() {
        (new AttackNearbyEnemies(unit)).forceHandle();

        return usedManager(this);
    }
}
