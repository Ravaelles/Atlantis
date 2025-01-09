package atlantis.information.enemy;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class OurBuildingSurroundedByEnemies {
    public static AUnit get() {
        for (AUnit unit : Select.ourBuildingsWithUnfinished().list()) {
            if (unit.enemiesNear().combatUnits().countInRadius(7, unit) > 0) return unit;
        }

        return null;
    }

    public static boolean notNull() {
        return get() != null;
    }

    public static boolean none() {
        return get() == null;
    }
}
