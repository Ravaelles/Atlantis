package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
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
    public boolean notifyNearUnits(HasPosition runFrom) {
        if (runFrom == null || !runFrom.hasPosition()) return false;
        if (We.protoss() && unit.friendsNear().inRadius(0.4, unit).atMost(1)) return false;
        if (unit.isFlying() || unit.isLoaded()) return false;

        Selection friendsTooClose = unit
            .friendsNear()
            .groundUnits()
            .nonBuildings()
            .notRunning()
            .realUnits()
            .exclude(unit)
            .inRadius(unit.isNotLarge() ? NOTIFY_UNITS_IN_RADIUS : 0.6, unit);

        if (unit.friendsNear().groundUnits().inRadius(1, unit).atMost(1)) return false;

        for (AUnit otherUnit : friendsTooClose.list()) {
            if (canBeNotifiedToMakeSpace(otherUnit)) {
//                A.errPrintln(A.minSec() + " Notify: " + unit + " is notifying " + otherUnit + " to make space");
                if (otherUnit.moveAwayFrom(runFrom, 0.5, Actions.MOVE_SPACE)) {
                    APainter.paintCircleFilled(unit, 10, Color.Yellow);
                    APainter.paintCircleFilled(otherUnit, 7, Color.Grey);
                    otherUnit.setTooltip("MakeSpace" + A.dist(otherUnit, unit), false);
                }
            }
        }

        return true;
    }

    private boolean canBeNotifiedToMakeSpace(AUnit unit) {
        if (unit.isRunning() || unit.isGroundUnit()) return false;
        if (unit.lastCommandIssuedAgo() <= 1) return false;

        if (unit.isTerranInfantry()) return true;

        return !unit.isStartingAttack()
            && !unit.isAttackFrame()
            && unit.lastStartedRunningMoreThanAgo(3)
            && !unit.isTankSieged()
            && !unit.type().isReaver();
    }
}
