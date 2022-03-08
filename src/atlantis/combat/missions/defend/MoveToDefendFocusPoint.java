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

    private static final double _300_MODE_DIST_FROM_FOCUS = 0.55;

    public static boolean move(AUnit unit, AFocusPoint focusPoint) {
        MoveToDefendFocusPoint.unit = unit;
        MoveToDefendFocusPoint.focusPoint = focusPoint;

        if (unit.distToSquadCenter() >= 8 && unit.meleeEnemiesNearCount() == 0) {
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

        if (unit.enemiesNear().inRadius(1.1, unit).isNotEmpty()) {
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
        if (nearestPoint != null) {
            double dist = nearestPoint.distTo(unit);
            double baseDist = _300_MODE_DIST_FROM_FOCUS;
            if (baseDist <= dist && dist <= baseDist + 0.08 && !unit.isAttacking()) {
                String tooltip = "300";
                unit.holdPosition(tooltip, true);
                unit.addLog(tooltip);
                return true;
            }
        }

        return false;
    }

    protected static double optimalDist() {
        if (unit.isZealot()) {
            return _300_MODE_DIST_FROM_FOCUS + letWorkersComeThroughBonus();
        }

        double base = Enemy.protoss() ? 0.6 : 0.0;

        if (unit.isTerran()) {
            base += (unit.isTank() ? 3 : 0)
                + (unit.isMedic() ? -2.5 : 0)
                + (unit.isMarine() ? 2 : 0)
                + (Select.our().inRadius(2, unit).count() / 25.0);
        }

        return base
            + letWorkersComeThroughBonus()
            + rangedDistBonus();
    }

    private static double letWorkersComeThroughBonus() {
        return unit.enemiesNear().combatUnits().isEmpty()
                && Select.ourWorkers().inRadius(7, unit).atLeast(1)
                ? 3 : 0;
    }

    private static double rangedDistBonus() {
        if (unit.isDragoon()) {
            return 1.7;
        }

        return (unit.isRanged() ? 3 : 0);
    }

}
