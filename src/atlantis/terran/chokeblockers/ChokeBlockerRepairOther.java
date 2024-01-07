package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class ChokeBlockerRepairOther extends Manager {
    private final AUnit otherBlocker;

    public ChokeBlockerRepairOther(AUnit unit) {
        super(unit);
        this.otherBlocker = ChokeBlockersAssignments.get().otherBlocker(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && otherBlocker != null && otherBlocker.isWounded();
    }

    public Manager handle() {
        unit.repair(otherBlocker, "RepairChoker");
        return usedManager(this);
    }
}
