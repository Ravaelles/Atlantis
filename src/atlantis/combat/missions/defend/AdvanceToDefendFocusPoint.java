package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.ContainFocusPoint;
import atlantis.combat.advance.focus.MoveToFocusPoint;
import atlantis.combat.squad.positioning.TooClustered;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.We;

public class AdvanceToDefendFocusPoint extends MoveToFocusPoint {
    public AdvanceToDefendFocusPoint(AUnit unit) {
        super(unit);
    }

//    @Override
//    public Manager handle() {
//        if (move()) return usedManager(this);
//
//        return null;
//    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            TooClustered.class,
            ContainFocusPoint.class,
        };
    }

//    protected boolean advance() {
//        focus = unit.mission().focusPoint();
//
//        if (focus == null || !focus.hasPosition()) {
////            System.err.println("Null focus point for " + unit + " in Advance");
//            System.err.println("unit.mission() = " + unit.mission());
//            A.printStackTrace("Null focus point for " + unit + " in Advance");
//            return false;
//        }
//
////        if (unit.enemiesNear().inRadius(5, unit).notEmpty()) {
////            if (!unit.isZergling() || unit.hp() >= 20) {
////                unit.addLog("DontWithdraw");
////                return false;
////            }
////        }
//
//        if (unitToFocus > (optimalDist + MARGIN)) {
//            String dist = A.dist(unitToFocus);
//
//            if (unit.lastActionMoreThanAgo(20, Actions.MOVE_FOCUS)) {
//                APosition position =
//                    (unit.distTo(focus) <= 6 ||
//                    (
//                        focus.region() != null && focus.region().equals(unit.position().region()))
//                        ? focus.translatePercentTowards(unit, 40) : focus
//                    );
//                return unit.move(
//                    position,
//                    Actions.MOVE_FOCUS,
//                    "ToFocus" + dist,
//                    true
//                );
//            }
//        }
//
//        return false;
//    }

    // =========================================================


    public double optimalDist() {
//        if (unit.isZealot()) {
//            private final double SPARTA_MODE_DIST_FROM_FOCUS = 0.55;
//            return SPARTA_MODE_DIST_FROM_FOCUS + letWorkersComeThroughBonus();
//        }

        double base = 0.0;

        if (We.zerg() && Enemy.protoss()) {
            base = 0.6;
        }

        if (unit.isTerran()) {
            base += (unit.isTank() ? 2.5 : 0)
                + (unit.isMedic() ? -2.5 : 0)
                + (unit.isFirebat() ? -1.5 : 0)
                + (unit.isRanged() ? 1 : 0)
                + Math.min(4, (Select.our().combatUnits().inRadius(8, unit).count() / 6));

            if (We.zerg() && focusPoint.isAroundChoke()) {
                base += (focusPoint.choke().width() <= 3) ? 3.5 : 0;
            }
        }

        return Math.max(
            (unit.isRanged() ? 3.7 : 0),
            base
                + letWorkersComeThroughBonus()
                + (unit.isDragoon() ? 1.7 : 0)
        );
    }

    private double letWorkersComeThroughBonus() {
        if (We.protoss() && A.seconds() >= 150) {
            return 0;
        }

        return unit.enemiesNear().combatUnits().isEmpty()
            && Select.ourWorkers().inRadius(7, unit).atLeast(1)
            ? 3 : 0;
    }

}
