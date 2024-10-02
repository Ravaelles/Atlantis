package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Order;

public class UnfreezeDragoon extends Manager {
    public UnfreezeDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        return unit.isDragoon()
//            && (unit.isStopped() || unit.u().getOrder().equals(Order.Stop))
//            && unit.noCooldown()
//            && !unit.isHoldingPosition()
//            && (unit.isStopped() || unit.u().getOrder().equals(Order.Stop))
//            && unit.lastActionMoreThanAgo(20, Actions.STOP);
            && (unit.isMoving() || unit.isStopped())
            && unit.lastPositionChangedMoreThanAgo(70);
//            && unit.noCooldown();
//            && unit.lastActionMoreThanAgo(20, Actions.HOLD_POSITION);
    }

    @Override
    public Manager handle() {
//        unit.holdPosition("UnfreezeHold");
//        System.err.println(A.nowString() + " UnfreezeDragoon#" + unit.id());
        if (unit.lastActionMoreThanAgo(21, Actions.STOP)) {
            if (unit.lastActionMoreThanAgo(11, Actions.STOP)) {
                unit.stop("UnfreezeDragoon");
            }

            return usedManager(this);
        }

        return null;
    }
}
