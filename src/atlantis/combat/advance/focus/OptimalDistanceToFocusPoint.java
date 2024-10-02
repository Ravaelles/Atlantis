package atlantis.combat.advance.focus;

import atlantis.combat.squad.alpha.Alpha;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.We;

public class OptimalDistanceToFocusPoint {
    public static double forUnit(AUnit unit, AFocusPoint focusPoint) {
        if (unit.isProtoss()) return asProtoss(unit, focusPoint);
        if (unit.isTerran()) return asTerran(unit, focusPoint);
        if (unit.isZerg()) return asZerg(unit, focusPoint);

//        return 4;
        return 0.5;
    }

    private static double asProtoss(AUnit unit, AFocusPoint focusPoint) {
        if (Alpha.count() <= 3) return 1.5;

        double rangedBonus = (unit.isMissionDefend() && unit.isRanged() ? 1.3 : 0);

        if (unit.isMissionDefend()) return 4 + rangedBonus;

        return (focusPoint != null && focusPoint.chokeWidthOr(99) <= 5)
            ? (8 + rangedBonus)
            : (7 + rangedBonus);
    }

    // =========================================================

    private static double asZerg(AUnit unit, AFocusPoint focusPoint) {
        if (unit.isMelee()) return 2;

        double base = 3;

        if (focusPoint.isAroundChoke()) {
            base += (focusPoint.choke().width() <= 3) ? 3.5 : 0;
        }

        return base;
    }

    // =========================================================

    private static double asTerran(AUnit unit, AFocusPoint focusPoint) {
        double base = 0.0;

        if (unit.isMedic()) return 0.5;
        if (unit.isMelee()) return 2;

        if (We.zerg() && Enemy.protoss()) {
            base = 0.6;
        }

        if (unit.isTerran()) {
            base += (unit.isTank() ? 2.5 : 0)
                + (unit.isMedic() ? -2.5 : 0)
                + (unit.isFirebat() ? -1.5 : 0)
                + (unit.isRanged() ? 1 : 0)
                + Math.min(4, (Select.our().combatUnits().inRadius(8, unit).count() / 6));
        }

        return base;
    }

    // =========================================================

//    public static double toFocus(AUnit unit, AFocusPoint focusPoint) {
//        if (focusPoint.isAroundChoke() && !Missions.isGlobalMissionSparta()) {
////            return 15 - (unit.isMelee() ? 1.5 : 0);
//            return 15 - (unit.isMelee() ? 1.5 : 0);
//        }
//
//        return 0;
//    }
}
