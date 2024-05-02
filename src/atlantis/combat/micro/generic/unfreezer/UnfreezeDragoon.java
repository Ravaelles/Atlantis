package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Order;

public class UnfreezeDragoon extends Manager {
    public UnfreezeDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDragoon()
            && (unit.isStopped() || unit.u().getOrder().equals(Order.Stop))
            && unit.lastActionMoreThanAgo(20, Actions.STOP);
    }

    @Override
    public Manager handle() {
        unit.stop("UnfreezeStop");
        return usedManager(this);
    }
}
