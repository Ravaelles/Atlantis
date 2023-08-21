package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class AttackTargetInRangeIfRanTooLong extends AttackTargetInRange {
    public AttackTargetInRangeIfRanTooLong(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.enemiesNear().inShootRangeOf(unit).empty()) return false;

        if (unit.noCooldown() && unit.lastStartedRunningLessThanAgo(30 * 3) && unit.hp() >= 70) return true;

        return
            (unit.lastStartedAttackAgo() > 85 || unit.lastStartedRunningAgo() < unit.lastStartedAttackAgo())
                && unit.enemiesNear().effVisible().notEmpty()
                && (unit.hp() >= 80 || (unit.hp() >= 45 && TerranWraith.noAntiAirBuildingNearby(unit)));
    }
}

