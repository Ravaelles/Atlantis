package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.terran.repair.RepairAssignments;
import atlantis.terran.repair.RepairableUnits;
import atlantis.units.AUnit;

public class DoRepairsNearby extends Manager {

    private AUnit target;

    public DoRepairsNearby(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isRepairing() && unit.distToTargetLessThan(1.5)) return false;

        target = RepairableUnits.get().nearestTo(unit);
        return target != null
            && target.distTo(unit) < (target.isAir() ? 2 : 6)
            && RepairAssignments.countRepairersForUnit(target) < 6
            && preventAwfulStacks();
    }

    private boolean preventAwfulStacks() {
        int workersNear = unit.friendsNear().workers().inRadius(5, unit).count();

        return workersNear <= 4 || unit.id() % 3 == 0 || unit.id() % 5 == 0;
    }

    @Override
    public Manager handle() {
        if (repairNearbyUnit()) return usedManager(this);

        return null;
    }

    private boolean repairNearbyUnit() {
        unit.repair(target, "NearbyRepair");

        return false;
    }


    private boolean repairIfNtTooManyRepairersAlready(AUnit target, String tooltip) {
        if (RepairAssignments.countRepairersForUnit(target) < 6) {
            unit.setTooltip(tooltip);
            RepairAssignments.addRepairer(unit, target);

            return true;
        }

        return false;
    }
}
