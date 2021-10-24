package atlantis.enemy;

import atlantis.units.AUnit;
import atlantis.units.Select;

public class NearestEnemy {

    public static boolean rangedHasSmallerRangeThan(AUnit unit) {
        AUnit enemy = Select.enemyCombatUnits().ranged().canShootAt(unit, 2.5).nearestTo(unit);

        if (enemy != null) {
            return unit.hasBiggerRangeThan(enemy);
        }
        return false;
    }

}
