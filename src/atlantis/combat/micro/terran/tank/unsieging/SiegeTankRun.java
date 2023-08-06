package atlantis.combat.micro.terran.tank.unsieging;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.tank.TerranTank;
import atlantis.units.AUnit;

public class SiegeTankRun extends Manager {
    public SiegeTankRun(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTankSieged();
    }

    public Manager handle() {
        if (
            unit.lastUnderAttackLessThanAgo(30)
                && (unit.hp() >= 100 || unit.enemiesNearInRadius(2) <= 2)
        ) {
            if (unit.enemiesNear().groundUnits().inRadius(3, unit).count() >= (unit.hpPercent() >= 50 ? 2 : 1)) {
                unit.setTooltip("Evacuate");
                TerranTank.wantsToUnsiege(unit);
                return usedManager(this);
            }
        }

        return null;
    }
}