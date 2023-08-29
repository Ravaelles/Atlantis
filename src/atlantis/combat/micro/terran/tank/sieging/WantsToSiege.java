package atlantis.combat.micro.terran.tank.sieging;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class WantsToSiege {
    public static boolean wantsToSiegeNow(AUnit unit, String tooltip) {
        if ((new WouldBlockChokeBySieging(unit)).invoke() != null) return false;
        if (unit.lastStartedRunningLessThanAgo(30 * (3 + unit.id() % 4))) return false;

        if (!Enemy.terran()) {
            if (unit.friendsNear().tanksSieged().inRadius(1.2, unit).isNotEmpty()) {
                return false;
            }

            // Prevent tanks from blocking chokes
            if (
                unit.enemiesNear().combatBuildingsAntiLand().inRadius(8, unit).empty()
                    && unit.distToNearestChokeLessThan(1.7)
            ) {
                return false;
            }
        }

        unit.siege();

        unit.setTooltipTactical(tooltip);
        unit.addLog(tooltip);
        return true;
    }
}
