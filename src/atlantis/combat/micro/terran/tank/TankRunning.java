package atlantis.combat.micro.terran.tank;

import atlantis.units.AUnit;
import atlantis.units.managers.Manager;

public class TankRunning extends Manager {

    public TankRunning(AUnit unit) {
        super(unit);
    }

    public Manager handle() {
        if (
            unit.lastUnderAttackLessThanAgo(30)
                && (unit.hp() >= 100 || unit.enemiesNearInRadius(2) <= 2)
        ) {
            if (unit.enemiesNear().groundUnits().inRadius(3, unit).count() >= (unit.hpPercent() >= 50 ? 2 : 1)) {
                unit.setTooltip("Evacuate");
                unit.unsiege();
                return usingManager(this);
            }
        }

        return null;
    }
}