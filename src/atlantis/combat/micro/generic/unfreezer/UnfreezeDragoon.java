package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.combat.advance.focus.HandleFocusPointPositioning;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;
import bwapi.Order;

public class UnfreezeDragoon extends Manager {
    public UnfreezeDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        return unit.isDragoon()
//            && !unit.isAttacking()
            && unit.noCooldown()
//            && (unit.isStopped() || unit.u().getOrder().equals(Order.Stop))
//            && !unit.isHoldingPosition()
            && (unit.isStopped() || unit.u().getOrder().equals(Order.Stop) || unit.lastPositionChangedMoreThanAgo(120))
//            && unit.u().isSt
//            && (unit.isStopped() || unit.isHoldingPosition())
//            && unit.lastActionMoreThanAgo(20, Actions.STOP)
//            && (unit.isMoving() || unit.isStopped())
            && unit.lastPositionChangedMoreThanAgo(70);
//            && unit.noCooldown();
//            && unit.lastActionMoreThanAgo(20, Actions.HOLD_POSITION);
    }

    @Override
    public Manager handle() {
//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - UnfreezeDragoon");
//        unit.paintCircleFilled(10, Color.Blue);

        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

//        if (unit.lastActionMoreThanAgo(81, Actions.STOP)) {
//            unit.stop("StopA");
//            return usedManager(this, "UnfreezeGoonA");
//        }
//
//        if (unit.move(unit.position().translateByPixels(4, 4), Actions.MOVE_UNFREEZE, "UnfreezeGoonB")) {
//            return usedManager(this);
//        }

//        if (unit.lastActionMoreThanAgo(91, Actions.HOLD_POSITION)) {
//            unit.holdPosition("HoldB");
//            return usedManager(this, "UnfreezeGoonA");
//        }

        return usedManager(this, "UnfreezeGoonC");

//        if (unit.lastActionMoreThanAgo(11, Actions.HOLD_POSITION)) {
//            if (unit.holdPosition("Hold")) return usedManager(this);
//        }
//        else {
//            return usedManager(this);
//        }

//        if (moveToLeader()) return usedManager(this);
//        if ((new HandleFocusPointPositioning(unit)).invokeFrom(this) != null) return usedManager(this);
    }

    private boolean moveToLeader() {
        AUnit leader = unit.squadLeader();
        if (leader == null) return false;

        if (unit.distTo(leader) >= 2.5) {
            if (unit.move(leader, Actions.MOVE_UNFREEZE, "FixIdleByLeader")) {
//                System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.targetPosition() +
//                    " / " + unit.action());
                return true;
            }
        }
        return false;
    }
}
