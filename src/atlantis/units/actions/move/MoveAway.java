package atlantis.units.actions.move;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
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
//            ErrorLog.printErrorOnce("MoveAway returned unwalkable " + newPosition + ", return false (" + unit.name() + ")");
            return false;
        }

        if (
            unit.runningManager().isReasonablePositionToRun(unit, newPosition)
                && unit.move(newPosition, action, "Move away", false)
//                && (unit().isAir() || Select.all().groundUnits().inRadius(0.05, newPosition).empty())
        ) {
            double distTo = unit.distTo(newPosition);
            if (distTo >= 1.9 && distTo > 3 * moveDistance) {
                if (moveDistance != 2.5) return unit.moveAwayFrom(from, 2.5, action, tooltip);

                ErrorLog.printMaxOncePerMinute(
                    unit.typeWithUnitId() + "::moveAwayFrom: distTo: " + distTo
                        + " / " + "moveDistance: " + moveDistance
                );
                return false;
            }
        }
        unit.setTooltip(tooltip, false);

        return true;

//        this.setTooltip("CantMoveAway", false);
//        APainter.paintCircle(this, 3, Color.Red);
//        APainter.paintCircle(this, 5, Color.Red);
//        APainter.paintCircle(this, 7, Color.Red);
//        APainter.paintCircle(this, 9, Color.Red);
//        return false;
//        return move(newPosition, Actions.MOVE_ERROR, "Force move", false);
    }
}
