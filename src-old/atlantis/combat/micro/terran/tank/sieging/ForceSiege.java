package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ForceSiege {
    public static Manager forceSiegeNow(Manager manager, String tooltip) {
        AUnit unit = manager.unit();

        unit.siege();
        unit.setTooltipTactical(tooltip);
        unit.addLog(tooltip);

        return manager;
    }
}
