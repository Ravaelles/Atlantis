package atlantis.combat.advance.focus;

import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;

public class OptimalDistanceToFocusPoint {
    public static double forUnit(AUnit unit) {
        if (unit.isDragoon()) return ChokeToBlock.DIST_FROM_CHOKE_CENTER + 3.0;

        if (unit.isMedic()) return 0.5;
        if (unit.isMelee()) return 2;
        return 4;
    }
}
