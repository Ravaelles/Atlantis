package atlantis.units.workers;

import atlantis.architecture.Manager;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;

public class CrucialRepairsNearby extends Manager {
    public CrucialRepairsNearby(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isRepairing();
    }

    @Override
    public Manager handle() {
        if (repairNearbyCrucialUnit()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean repairNearbyCrucialUnit() {
        for (AUnit target : unit.friendsNear().mechanical().wounded().list()) {
            if (ifShouldRepairNowThenRepair(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean ifShouldRepairNowThenRepair(AUnit target) {
        if (target.isTank() && target.distTo(unit) < target.woundPercent() / 15) {
            return repairIfNtTooManyRepairersAlready(target, "SaveTank");
        }

        if (target.isMissileTurret() && target.distTo(unit) < 10) {
            return repairIfNtTooManyRepairersAlready(target, "PrayForTurret");
        }

        if (target.isBunker() && target.distTo(unit) < 10) {
            return repairIfNtTooManyRepairersAlready(target, "SaveBunker");
        }

        if (target.distTo(unit) < 1.4) {
            return repairIfNtTooManyRepairersAlready(target, "HelpHim");
        }

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
