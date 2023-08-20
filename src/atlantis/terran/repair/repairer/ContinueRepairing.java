package atlantis.terran.repair.repairer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ContinueRepairing extends Manager {
    public ContinueRepairing(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.lastActionLessThanAgo(5, Actions.REPAIR);
    }

    @Override
    public Manager handle() {
        unit.setTooltip(":: repairin ::");
        return usedManager(this);
    }
}
