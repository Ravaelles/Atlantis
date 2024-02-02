package atlantis.combat.advance.focus;

import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class OptimalDistanceToFocusPoint {
    public static double forUnit(AUnit unit) {
        if (unit.isDragoon()) {
            return ChokeToBlock.DIST_FROM_CHOKE_CENTER
                + 3.5
                + (unit.friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(0.5, unit).notEmpty() ? 2 : 0);
        }
        ;

        if (unit.isMedic()) return 0.5;
        if (unit.isMelee()) return 2;
        return 4;
    }
}
