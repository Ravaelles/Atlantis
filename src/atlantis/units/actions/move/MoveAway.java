package atlantis.units.actions.move;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class MoveAway {
    public static boolean from(AUnit unit, HasPosition from, double moveDistance, Action action, String tooltip) {
        if (from == null || moveDistance < 0.01) return false;

        int dx = from.x() - unit.x();
        int dy = from.y() - unit.y();
        double vectorLength = Math.sqrt(dx * dx + dy * dy);
        double modifier = (moveDistance * 32) / vectorLength;
        dx = (int) (dx * modifier);
        dy = (int) (dy * modifier);

        APosition newPosition = new APosition(unit.x() - dx, unit.y() - dy).makeValidGroundPosition();

        if (!unit.isFlying()) {
            APosition freeFromUnits = newPosition.makeWalkableAndFreeOfAnyGroundUnits(3, 0.15, unit);
            if (freeFromUnits != null) {
                newPosition = freeFromUnits;
            }
        }

        if (newPosition == null) {
            ErrorLog.printErrorOnce("Cannot moveAwayFrom " + from + " for " + unit.name());
            return false;
        }

        if (unit.isGroundUnit() && !newPosition.isWalkable()) {
            if (!unit.isWorker()) {
                ErrorLog.printErrorOnce(
                    "MoveAway returned unwalkable " + newPosition + ", return false (" + unit.name() + ")"
                );
            }
            return false;
        }

        double distTo = unit.distTo(newPosition);
        boolean positionOk = (distTo >= 0.02 || distTo <= 3 * moveDistance);
//            boolean positionOk = (moveDistance <= 2 && distTo >= 0.02)
//                || (distTo >= 1.9 && distTo > 3 * moveDistance && !unit.isWorker());

        if (
//            unit.runningManager().isReasonablePositionToRun(unit, newPosition)
            positionOk
        ) {
//            if (from instanceof AUnit && Select.enemy().bunkers().countInRadius(0.5, from) > 0) {
//                A.printStackTrace("Why running from bunker?");
//            }

//                if (moveDistance < 2.5) return unit.moveAwayFrom(from, 2.5, action, tooltip);
//                return unit.moveAwayFrom(from, 2.5, action, tooltip);

//                ErrorLog.printMaxOncePerMinute(
//                    unit.typeWithUnitId() + "::moveAwayFrom: distTo: " + distTo
//                        + " / " + "moveDistance: " + moveDistance
//                );
//                return false;

            if (unit.move(newPosition, action, "Move away", false)) {
                return true;
            }
            else {
                if (A.isUms() && !unit.isABuilding()) {
                    System.err.println(
                        "MoveAway failed, dist: " + distTo + " / prefered:" + moveDistance + " / " + unit
                    );
                }
                return true;
            }
        }
        else {
            System.err.println(
                "MoveAway: not ok " + unit.distTo(newPosition) + " / " + newPosition.isWalkable() + " / " + unit
            );
        }

        return false;

//        this.setTooltip("CantMoveAway", false);
//        APainter.paintCircle(this, 3, Color.Red);
//        APainter.paintCircle(this, 5, Color.Red);
//        APainter.paintCircle(this, 7, Color.Red);
//        APainter.paintCircle(this, 9, Color.Red);
//        return false;
//        return move(newPosition, Actions.MOVE_ERROR, "Force move", false);
    }
}
