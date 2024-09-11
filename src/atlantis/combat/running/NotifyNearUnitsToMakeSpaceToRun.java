package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.We;
import bwapi.Color;

public class NotifyNearUnitsToMakeSpaceToRun extends HasUnit {
    public static double NOTIFY_UNITS_MAKE_SPACE = 0.75;
    public static double NOTIFY_UNITS_IN_RADIUS = 0.2;

    public NotifyNearUnitsToMakeSpaceToRun(AUnit unit) {
        super(unit);
    }

    /**
     * Tell other units that might be blocking our escape route to move.
     */
    public boolean notifyNearUnits() {
        if (We.protoss() && unit.friendsNear().inRadius(0.35, unit).atMost(1)) return false;

        if (unit.isFlying() || unit.isLoaded()) return false;

//        if (unit.enemiesNear().melee().inRadius(4, unit).empty()) {
//            return false;
//        }

        Selection friendsTooClose = unit
            .friendsNear()
            .groundUnits()
            .notRunning()
            .realUnits()
            .exclude(unit)
            .inRadius(NOTIFY_UNITS_IN_RADIUS, unit);

        if (friendsTooClose.count() <= 1) return false;

        for (AUnit otherUnit : friendsTooClose.list()) {
            if (canBeNotifiedToMakeSpace(otherUnit)) {
                AUnit runFrom = otherUnit.enemiesNear().nearestTo(otherUnit);
                if (runFrom == null || !runFrom.hasPosition()) {
                    continue;
                }

//                System.err.println(otherUnit + " // notified by " + unit + " (" + unit.hp() + ")");

                otherUnit.runningManager().runFrom(runFrom, NOTIFY_UNITS_MAKE_SPACE, Actions.MOVE_SPACE, true);
                APainter.paintCircleFilled(unit, 10, Color.Yellow);
                APainter.paintCircleFilled(otherUnit, 7, Color.Grey);
                otherUnit.setTooltip("MakeSpace" + A.dist(otherUnit, unit), false);
            }
        }
        return true;
    }

    private boolean canBeNotifiedToMakeSpace(AUnit unit) {
        return !unit.isRunning()
            && unit.isGroundUnit()
            && unit.lastStartedRunningMoreThanAgo(3)
            && !unit.isTankSieged()
            && !unit.type().isReaver();
    }
}
