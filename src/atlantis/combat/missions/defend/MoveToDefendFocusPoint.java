package atlantis.combat.missions.defend;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MoveToFocusPoint;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.Enemy;
import atlantis.util.We;

public class MoveToDefendFocusPoint extends MoveToFocusPoint {

    public static boolean move(AUnit unit, AFocusPoint focusPoint) {
        MoveToDefendFocusPoint.unit = unit;
        MoveToDefendFocusPoint.focusPoint = focusPoint;

//        if (holdOnPerpendicularLine()) {
//            return true;
//        }

        fromSide = focusPoint.fromSide();
        optimalDist = optimalDist();
        distUnitToFocus = unit.distTo(focusPoint);
        distUnitToFromSide = focusPoint.fromSide() == null ? -1 : unit.distTo(focusPoint.fromSide());
        distFocusToFromSide = focusPoint.fromSide() == null ? -1 : focusPoint.distTo(focusPoint.fromSide());
//
//        if (tooFar() || tooClose()) {
//            return true;
//        }

        if (wrongSideOfFocus() || tooCloseToFocusPoint() || joinSquad(unit) || advance()) {
            return true;
        }

        return false;
    }

    private static boolean joinSquad(AUnit unit) {
        if (unit.distToSquadCenter() >= 8 && unit.enemiesNear().isEmpty()) {
            unit.addLog("JoinSquad");
            return unit.move(unit.squadCenter(), Actions.MOVE_FORMATION, "JoinSquad", false);
        }
        return false;
    }

    // =========================================================

    protected static double optimalDist() {
//        if (unit.isZealot()) {
//            private static final double SPARTA_MODE_DIST_FROM_FOCUS = 0.55;
//            return SPARTA_MODE_DIST_FROM_FOCUS + letWorkersComeThroughBonus();
//        }

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
        if (We.protoss() && A.seconds() >= 150) {
            return 0;
        }

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
