package atlantis.information.enemy;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class OurBuildingUnderAttack {
    public static AUnit get() {
//        for (AUnit unit : Select.ourBasesWithUnfinished().list()) {
        for (AUnit unit : Select.ourBuildingsWithUnfinished().list()) {
            if (unit.lastUnderAttackLessThanAgo(60)) return unit;
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
