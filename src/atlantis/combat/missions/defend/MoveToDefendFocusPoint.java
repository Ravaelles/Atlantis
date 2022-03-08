package atlantis.combat.missions.defend;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MoveToFocusPoint;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class MoveToDefendFocusPoint extends MoveToFocusPoint {

    protected static final double MARGIN = 0.1;

    public static boolean move(AUnit unit, AFocusPoint focusPoint) {
        MoveToDefendFocusPoint.unit = unit;
        MoveToDefendFocusPoint.focusPoint = focusPoint;

        if (unit.distToSquadCenter() >= 8) {
            unit.addLog("JoinSquad");
            return unit.move(unit.squadCenter(), Actions.MOVE_FORMATION, "JoinSquad", false);
        }

        fromSide = focusPoint.fromSide();
        optimalDist = optimalDist();
        distUnitToFocus = unit.distTo(focusPoint);
        distUnitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
        distFocusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());
//
//        if (tooFar() || tooClose()) {
//            return true;
//        }

        if (wrongSideOfFocus() || holdOnPerpendicularLine() || tooCloseToFocusPoint() || advance()) {
            return true;
        }

//        if () {
//            return true;
//        }

//        unit.holdPosition("Sparta", true);
//        unit.addLog("Sparta");
        return false;
    }

    // =========================================================

    private static boolean holdOnPerpendicularLine() {
//        if (!Enemy.zerg()) {
//            return false;
//        }

        if (!unit.isMelee()) {
            return false;
        }

        if (unit.enemiesNear().inRadius(1.2, unit).isNotEmpty()) {
            return false;
        }

        if (focusPoint.choke() == null) {
            return false;
        }

        if (focusPoint.choke().perpendicularLine().isEmpty()) {
            System.err.println("Undefined focusPoint choke perpendicularLine");
            return false;
        }

        HasPosition nearestPoint = (new Positions(focusPoint.choke().perpendicularLine())).nearestTo(unit);
//        System.out.println("nearestPoint = " + nearestPoint + " // " + focusPoint.choke().perpendicularLine().size());
//        if (nearestPoint != null) {
//            System.out.println("dist = " +  nearestPoint.distTo(unit));
//        }
        if (nearestPoint != null) {
            double dist = nearestPoint.distTo(unit);
            double baseDist = 0.5;
            if (baseDist <= dist && dist <= baseDist + 0.08 && !unit.isAttacking()) {
                String tooltip = "300";
                unit.holdPosition(tooltip, true);
                unit.addLog(tooltip);
                return true;
            }
        }

//        double preferedDist = 0.8;
//        double margin = 0.1;
//        double distToFirstPoint = focusPoint.choke().firstPoint().distTo(unit);
//        double distToLastPoint = focusPoint.choke().lastPoint().distTo(unit);
//        if (
//                Math.abs(distToFirstPoint - preferedDist) <= margin
//                || Math.abs(distToLastPoint - preferedDist) <= margin
//        ) {
//            if (Math.abs(distToFirstPoint - distToLastPoint) >= 1) {
//                unit.holdPosition("300", true);
//                unit.addLog("300");
//                return true;
//            }
//        }

        return false;
    }

    protected static double optimalDist() {
        double base = Enemy.protoss() ? 1.6 : 0.0;

        if (unit.isTerran()) {
            base += (unit.isTank() ? 3 : 0)
                    + (unit.isMedic() ? -2.5 : 0)
                    + (unit.isMarine() ? 2 : 0)
                    + (Select.our().inRadius(2, unit).count() / 20.0);
        }

        return base
                + letWorkersComeThroughBonus()
                + rangedDistBonus();
    }

    private static double letWorkersComeThroughBonus() {
        return unit.enemiesNear().combatUnits().isEmpty()
                && Select.ourWorkers().inRadius(6, unit).atLeast(1)
                ? 3 : 0;
    }

    private static double rangedDistBonus() {
        if (unit.isDragoon()) {
            return 1.7;
        }

        return (unit.isRanged() ? 3 : 0);
    }

}
