package atlantis.units.actions.move;

import atlantis.battle.running.IsReasonablePositionToRunTo;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.select.Select;
import atlantis.util.AConsole;
import atlantis.util.log.ErrorLog;

public class MoveAway {
    public static boolean from(AUnit unit, HasPosition from, double moveDistance, Action action, String tooltip) {
        if (from == null || moveDistance < 0.01) return false;

        // Straight back first - identical to the old single try so the
        // common path cannot change under this refactor.
        if (tryStep(unit, from, moveDistance, action, tooltip, from)) return true;

        // A wall directly behind must not mean standing still (measured
        // Wine 1Drag_v_1Zeal: 106 stuck-no-step rows in a row, then
        // death): fan out laterally, never toward the threat. Each
        // candidate runs the same validation below, so a blocked
        // direction only costs a check. Rotating the threat point (not
        // the destination) keeps every candidate inside tryStep's
        // contract. A glued unit (dist ~0) has no defined away-direction;
        // the compass starts from an arbitrary axis.
        double dx = from.x() - unit.x();
        double dy = from.y() - unit.y();
        if (dx * dx + dy * dy < 0.01) {
            dx = 1;
            dy = 0;
        }
        double[] rotations = {
             0.7071, 0.7071, 0.7071, -0.7071,
             0.0, 1.0, 0.0, -1.0,
             -0.7071, 0.7071, -0.7071, -0.7071 };
        for (int i = 0; i < rotations.length; i += 2) {
            double rx = dx * rotations[i] - dy * rotations[i + 1];
            double ry = dx * rotations[i + 1] + dy * rotations[i];
            APosition rotatedThreat = new APosition(
                (int) (unit.x() + rx), (int) (unit.y() + ry)
            ).makeValidGroundPosition();
            if (tryStep(unit, rotatedThreat, moveDistance, action, tooltip, from)) return true;
        }
        return false;
    }

    /**
     * One step candidate: the shared validation + issue tail. The
     * geometric source varies (the threat for the straight step,
     * a compass point for lateral ones); the reasonableness check
     * always runs against the real threat, never the probe point.
     */
    private static boolean tryStep(AUnit unit, HasPosition aim, double moveDistance, Action action, String tooltip, HasPosition threat) {
        int dx = aim.x() - unit.x();
        int dy = aim.y() - unit.y();
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
            ErrorLog.printErrorOnce("Cannot moveAwayFrom " + aim + " for " + unit.name());
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
        boolean positionOk = IsReasonablePositionToRunTo.check(unit, newPosition, threat);
//        boolean positionOk = (
//            (distTo >= (0.9 * moveDistance)) || (distTo <= 3 * moveDistance)
//        ) && !newPosition.isCloseToMapBounds(1);
//            boolean positionOk = (moveDistance <= 2 && distTo >= 0.02)
//                || (distTo >= 1.9 && distTo > 3 * moveDistance && !unit.isWorker());

        if (
//            unit.runningManager().isReasonablePositionToRun(unit, newPosition)
            positionOk
        ) {
//            if (from instanceof AUnit && Select.enemy().bunkers().countInRadius(0.5, from) > 0) {
//                AConsole.printStackTrace("Why running from bunker?");
//            }

//                if (moveDistance < 2.5) return unit.moveAwayFrom(from, 2.5, action, tooltip);
//                return unit.moveAwayFrom(from, 2.5, action, tooltip);

//                ErrorLog.printMaxOncePerMinute(
//                    unit.typeWithUnitId() + "::moveAwayFrom: distTo: " + distTo
//                        + " / " + "moveDistance: " + moveDistance
//                );
//                return false;

            // A zero-length step is not a step: issuing it would look
            // like movement while standing glued in place.
            if (unit.distTo(newPosition) < 0.1) return false;

            if (unit.move(newPosition, action, "Move away" + unit.distToDigit(threat), false)) {
                return true;
            }
            else {
//                if (A.isUms() && !unit.isABuilding() && !unit.isAir()) {
//                    System.err.println(
//                        "MoveAway failed, dist: " + distTo + " / prefered:" + moveDistance + " / " + unit
//                    );
//                }
                return true;
            }
        }
//        else {
//            System.err.println(
//                "MoveAway: not ok " + unit.distTo(newPosition) + " / " + newPosition.isWalkable() + " / " + unit
//            );
//        }

        return false;
    }

//        this.setTooltip("CantMoveAway", false);
//        APainter.paintCircle(this, 3, Color.Red);
//        APainter.paintCircle(this, 5, Color.Red);
//        APainter.paintCircle(this, 7, Color.Red);
//        APainter.paintCircle(this, 9, Color.Red);
//        return false;
//        return move(newPosition, Actions.MOVE_ERROR, "Force move", false);
}
