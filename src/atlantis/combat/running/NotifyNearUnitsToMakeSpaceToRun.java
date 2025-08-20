package atlantis.combat.running;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.We;
import bwapi.Color;

public class NotifyNearUnitsToMakeSpaceToRun extends HasUnit {
    public NotifyNearUnitsToMakeSpaceToRun(AUnit unit) {
        super(unit);
    }

    /**
     * Tell other units that might be blocking our escape route to move.
     */
    public boolean notifyNearUnits(HasPosition runFrom) {
        if (runFrom == null || !runFrom.hasPosition()) return false;
        if (We.protoss()) {
            if (unit.hp() <= 40 || unit.enemiesNear().buildings().notEmpty()) return true;
            if (unit.friendsNear().inRadius(1, unit).atMost(1)) return false;
        }
        if (unit.isFlying() || unit.isLoaded()) return false;

        Selection friendsToNotify = friendsToNotify();

        if (unit.friendsNear().groundUnits().inRadius(1, unit).atMost(1)) return false;

        for (AUnit otherUnit : friendsToNotify.list()) {
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

    private Selection friendsToNotify() {
        Selection friends = unit.friendsNear()
            .groundUnits()
            .nonBuildings()
            .notRunning()
            .realUnits()
            .exclude(unit)
            .inRadius(radius(), unit);

        if (unit.nearestChokeDist() <= 5 && unit.eval() <= 2.5) {
            AChoke choke = unit.nearestChoke();
            if (choke != null) {
                friends = friends.add(
                    unit.friendsNear().groundUnits().notRunning().inRadius(3.5, choke.center())
                );
            }
        }

        return friends;
    }

    private double radius() {
        if (We.protoss()) {
            return 1.2;
        }
        else if (We.terran()) {
            return unit.isNotLarge() ? 0.2 : 0.6;
        }

        return 0.5;
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
