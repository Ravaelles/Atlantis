package atlantis.information.enemy;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class OurBuildingUnderAttack {
    public static AUnit get() {
        for (AUnit unit : Select.ourBasesWithUnfinished().list()) {
            if (unit.lastUnderAttackLessThanAgo(60)) return unit;
        }

        return null;
    }
}
