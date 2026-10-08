package atlantis.production.constructions.position.conditions.can_build_here;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class IsPoweredByAPylon {
    public static boolean check(APosition position) {
        return Select.ourOfType(AUnitType.Protoss_Pylon).countInRadius(3.2, position) > 0;

//        return pylons.countInRadius(5.3, position) > 0
//            && pylons.countInRadius(2.0, position) == 0;
    }
}
