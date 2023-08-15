package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class WantsToSiege {
    public static Manager wantsToSiegeNow(Manager manager, String tooltip) {
        AUnit unit = manager.unit();

        if ((new WouldBlockChokeBySieging(unit)).invoke() != null) {
            return null;
        }

        if (unit.lastStartedRunningLessThanAgo(30 * 5)) return null;

        if (!Enemy.terran()) {
            if (unit.friendsNear().tanksSieged().inRadius(1.2, unit).isNotEmpty()) {
                return null;
            }

            // Prevent tanks from blocking chokes
            if (
                unit.enemiesNear().combatBuildingsAntiLand().inRadius(8, unit).empty()
                    && unit.distToNearestChokeLessThan(1.7)
            ) {
                return null;
            }
        }

        unit.siege();

        unit.setTooltipTactical(tooltip);
        unit.addLog(tooltip);
        return manager;
    }
}
