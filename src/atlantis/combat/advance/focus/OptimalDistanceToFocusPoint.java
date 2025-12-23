package atlantis.combat.advance.focus;

import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import atlantis.units.select.Selection;
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
        if (focusPoint.hasIdealDistanceFromFocus()) {
            return focusPoint.idealDistanceFromFocus() + (unit.isMelee() ? 2 : 0);
        }

        if (We.protoss()) {
            if (unit.isRanged() && unit.friendsNear().melee().countInRadius(1.8, unit) == 0) {
                return 2;
            }
        }

        double totalBonus = totalBonus(unit);

        if (Alpha.count() <= 8 && focusPoint.isMainChoke()) return 0.5 + totalBonus;

        int chokeWidth = focusPoint.chokeWidthOr(99);
        return chokeWidth <= 5
            ? 8 + totalBonus
            : 7 + totalBonus;
    }

    private static double totalBonus(AUnit unit) {
        return
            (unit.isMissionDefendOrSparta() && unit.isRanged() ? 0.6 : 0) // Ranged bonus
            + meleeVsTerranBonus(unit)
            + letWorkersComeThroughBonus(unit);
    }

    private static double meleeVsTerranBonus(AUnit unit) {
        if (!Enemy.terran()) return 0;
        if (!unit.isMelee()) return 0;

        return 2.5;
    }

    private static double letWorkersComeThroughBonus(AUnit unit) {
//        if (We.protoss() && A.seconds() >= 150) {
//            return 0;
//        }

        if (unit.enemiesNear().combatUnits().countInRadius(13, unit) > 0) return 0;

        Selection workers = Select.ourWorkers();
        if (A.s % 7 <= 2) {
            workers = workers.notScout();
        }

        if (workers.inRadius(7, unit).empty()) return 0;

        return 3;
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
