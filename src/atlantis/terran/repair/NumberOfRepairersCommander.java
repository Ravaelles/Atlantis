package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import java.util.List;

public class NumberOfRepairersCommander extends Commander {
    @Override
    public void handle() {
        removeExcessiveRepairersIfNeeded();

        if (!OptimalNumOfRepairers.weHaveTooManyRepairersOverall()) {
            assignRepairersToWoundedUnits();
        }
    }

    protected void assignRepairersToWoundedUnits() {
        List<AUnit> repairable = Select.our()
            .repairable(true)
            .excludeTypes(AtlantisConfig.WORKER)
            .list();

        for (AUnit woundedUnit : repairable) {
//            if (woundedUnit.is(AUnitType.Terran_Missile_Turret)) {
//                System.out.println("Repair TURRET? ");
//            }

            if (ShouldNotRepairUnit.shouldNotRepairUnit(woundedUnit)) {
                continue;
            }

            if (OptimalNumOfRepairers.hasUnitTooManyRepairers(woundedUnit)) {
                continue;
            }

            int newRepairersNeeded = optimalNumOfRepairersFor(woundedUnit);
//            if (woundedUnit.is(AUnitType.Terran_Missile_Turret)) {
//                System.out.println("   HP=" + woundedUnit.hp() + " / repairers=" + newRepairersNeeded);
//            }
            if (newRepairersNeeded > 0) {
                assignRepairersToWoundedUnits(woundedUnit, newRepairersNeeded);
//                System.err.println("Assign " + newRepairersNeeded + " repairers to " + woundedUnit.name());
            }
        }
//        }
    }

    public boolean removeExcessiveRepairersIfNeeded() {
        int allowedRepairers = OptimalNumOfRepairers.MAX_REPAIRERS_AT_ONCE;

        if (OptimalNumOfRepairers.weHaveTooManyRepairersOverall()) {
            for (int i = 0; i < RepairAssignments.countTotalRepairers() - allowedRepairers; i++) {
                AUnit repairer = RepairAssignments.getRepairers().get(RepairAssignments.getRepairers().size() - 1);
                if (!CanAbandonUnitAssignedToRepair.check(repairer)) {
                    RepairAssignments.removeRepairer(repairer);
                }
//                System.err.println("Remove excessive repairer " + repairer);
            }
            return true;
        }

        return false;
    }

    public static int optimalNumOfRepairersFor(AUnit unit) {
        int alreadyAssigned = RepairAssignments.countRepairersForUnit(unit) + RepairAssignments.countProtectorsFor(unit);
        int repairersNeeded = 1;

        // === Bunker - very special case ========================================

        if (unit.isBunker()) {
            int shouldHaveThisManyRepairers = OptimalNumOfBunkerRepairers.forBunker(unit);
            if (shouldHaveThisManyRepairers > 0) {
//                System.out.println("Bunker repairers = " + shouldHaveThisManyRepairers);
                unit.setTooltipTactical(shouldHaveThisManyRepairers + " RepairNeed");
                ProtectorCommander.addProtectorsForUnit(unit, shouldHaveThisManyRepairers);
                return shouldHaveThisManyRepairers;
            }
            else {
                unit.removeTooltip();
            }
        }
        else if (unit.isMissileTurret()) {
            int enemies = unit.enemiesNear().air().inRadius(11, unit).count();

            if (Have.main() && Select.main().distToLessThan(unit, 14)) {
                return A.inRange(3, enemies, 5);
            }

            return A.inRange(2, (int) (enemies / 1.5), 5);
        }
        else if (unit.isTank()) {
            return 3;
        }

        return Math.max(0, repairersNeeded - alreadyAssigned);
    }

    protected boolean assignRepairersToWoundedUnits(AUnit unitToRepair, int numberOfRepairersToAssign) {
        for (int i = 0; i < numberOfRepairersToAssign; i++) {
            boolean isCriticallyImportant = unitToRepair.isTank() || unitToRepair.isBunker();
            AUnit worker = NewRepairer.repairerFor(unitToRepair, isCriticallyImportant);
            if (worker != null) {
                if (AGame.isUms() && worker.distTo(unitToRepair) > 10 && !worker.hasPathTo(unitToRepair)) return false;

                RepairAssignments.addRepairer(worker, unitToRepair);
                return true;
            }
        }
        return false;
    }
}
