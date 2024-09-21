package atlantis.combat.advance.focus;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.Enemy;

public class OptimalDistanceToFocusPoint {
    public static double forUnit(AUnit unit) {
        if (unit.isProtoss()) return asProtoss(unit);

        if (unit.isMedic()) return 0.5;
        if (unit.isMelee()) return 2;
//        return 4;
        return 0.5;
    }

    private static double asProtoss(AUnit unit) {
        if (Alpha.count() <= 3) return 2;

        AFocusPoint focusPoint = unit.focusPoint();
        int base = (focusPoint != null && focusPoint.chokeWidthOr(99) <= 5)
            ? 6 : 4;

        return base + (unit.isRanged() ? 1.3 : 0);

//        if (Enemy.zerg()) return 5;
//
//        if (unit.isDragoon()) {
//            return ChokeToBlock.BASE_DIST_FROM_CHOKE_CENTER
//                + 0.3
//                + (unit.friendsNear().ofType(AUnitType.Protoss_Zealot).inRadius(0.5, unit).notEmpty() ? 1.0 : 0);
//        }
//
//        int count = Alpha.count();
//        return count >= 6 ? (2 + count / 4.0) : 1;
    }

    public static double toFocus(AUnit unit, AFocusPoint focusPoint) {
        if (focusPoint.isAroundChoke() && !Missions.isGlobalMissionSparta()) {
            return 15 - (unit.isMelee() ? 1.5 : 0);
        }

        return 0;
    }
}
