package atlantis.combat.running.stop_running.terran;

import atlantis.units.AUnit;

public class ShouldStopRunningMarine {
    public static boolean shouldNotStop(AUnit unit) {
        if (!unit.isMarine()) return false;
        if (unit.isHealthy() && unit.hasMedicInRange()) return false;

        return unit.meleeEnemiesNearCount(2.1) > 0;
    }
}
