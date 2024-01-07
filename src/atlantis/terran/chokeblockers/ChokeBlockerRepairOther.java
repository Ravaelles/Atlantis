package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ChokeBlockerRepairOther extends Manager {
    private final AUnit otherToRepair;

    public ChokeBlockerRepairOther(AUnit unit) {
        super(unit);
        this.otherToRepair = otherBlockerToRepair(unit);
    }

    private static AUnit otherBlockerToRepair(AUnit unit) {
        return Select.from(ChokeBlockersAssignments.get().blockers, null)
            .exclude(unit)
            .workers()
            .wounded()
            .nearestTo(unit);
    }

    @Override
    public boolean applies() {
        return We.terran() && otherToRepair != null && otherToRepair.isWounded();
    }

    public Manager handle() {
        unit.repair(otherToRepair, "RepairChoker");
        return usedManager(this);
    }
}
