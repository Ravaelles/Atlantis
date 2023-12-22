package atlantis.combat.running;

import atlantis.units.AUnit;

public class StopRunningAsMarine {
    public static boolean shouldNotStop(AUnit unit) {
        if (!unit.isMarine()) return false;
        if (unit.isHealthy() && unit.hasMedicInRange()) return false;

        return unit.meleeEnemiesNearCount(2.1) > 0;
    }
}
