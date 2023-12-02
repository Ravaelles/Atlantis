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
            && RepairAssignments.countRepairersForUnit(target) < 6;
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

//    private boolean ifShouldRepairNowThenRepair(AUnit target) {
//        if (target.isWorker() && target.friendsNear().buildings().inRadius(10, target).empty()) return false;
//
//        if (target.isTank() && target.distTo(unit) < target.woundPercent() / 15) {
//            return repairIfNtTooManyRepairersAlready(target, "SaveTank");
//        }
//
//        if (target.isMissileTurret() && target.distTo(unit) < 10) {
//            return repairIfNtTooManyRepairersAlready(target, "PrayForTurret");
//        }
//
//        if (target.isBunker() && target.distTo(unit) < 10) {
//            return repairIfNtTooManyRepairersAlready(target, "SaveBunker");
//        }
//
//        if (target.distTo(unit) < 1.4) {
//            return repairIfNtTooManyRepairersAlready(target, "HelpHim");
//        }
//
//        return false;
//    }

    private boolean repairIfNtTooManyRepairersAlready(AUnit target, String tooltip) {
        if (RepairAssignments.countRepairersForUnit(target) < 6) {
            unit.setTooltip(tooltip);
            RepairAssignments.addRepairer(unit, target);
            return true;
        }

        return false;
    }
}
