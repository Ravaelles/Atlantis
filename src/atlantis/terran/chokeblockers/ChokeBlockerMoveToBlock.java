package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.OnWrongSideOfFocusPoint;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;
import bwapi.Color;

public class ChokeBlockerMoveToBlock extends Manager {
    private final APosition blockChokePosition;

    public ChokeBlockerMoveToBlock(AUnit unit) {
        super(unit);
        this.blockChokePosition = unit.specialPosition();
    }

    @Override
    public boolean applies() {
        if (blockChokePosition == null) return false;
        if (unit.isZealot() && unit.enemiesNearInRadius(1.15) > 0) return false;

        if (anyDragoonUnderAttack()) return false;
        if (unitsTryingToGetBackWithWrongSideOfChoke()) return false;

        return unit.hp() >= 25
            || unit.enemiesNear().inRadius(7, unit).groundUnits().havingAntiGroundWeapon().empty();
    }

    private boolean unitsTryingToGetBackWithWrongSideOfChoke() {
        for (AUnit friend : unit.friendsNear().groundUnits().inRadius(7, unit).list()) {
            if (friend.isMoving() && friend.isActiveManager(OnWrongSideOfFocusPoint.class)) {
                if (unit.moveToMain(Actions.MOVE_FORMATION)) {
                    unit.setTooltip("HelpUnblock");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean anyDragoonUnderAttack() {
        if (!We.protoss()) return false;

        return unit.squad().selection().dragoons().underAttackLessThanAgo(40).notEmpty();
    }

    public Manager handle() {
        double dist = unit.distTo(blockChokePosition);
        unit.paintLine(blockChokePosition, Color.White);
        unit.paintLine(blockChokePosition.translateByPixels(1, 1), Color.White);

        if (dist > 0.12 || (dist > 0.02 && !unit.isHoldingPosition())) {
//            if (unit.lastActionMoreThanAgo(7, Actions.SPECIAL) || dist >= 1) {
            if ((!unit.isMoving() && unit.lastPositionChangedMoreThanAgo(20)) || dist >= 1) {
                unit.move(blockChokePosition, Actions.SPECIAL, "ChokeBlocker");
            }
        }
        else {
            unit.holdPosition(Actions.HOLD_POSITION, "ChokeBlocker");
//            unit.repair(ChokeBlockersAssignments.get().otherBlocker(unit), "Hold!");
        }
        unit.setAction(Actions.SPECIAL);

        return usedManager(this);
    }
}
