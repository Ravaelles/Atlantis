package atlantis.terran.repair;

import atlantis.architecture.Commander;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.terran.repair.protect.ProtectorCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;
import bwapi.Color;

import java.util.List;

public class NewRepairsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.terran();
    }

    @Override
    protected void handle() {
        removeExcessiveRepairersIfNeeded();

        if (!OptimalNumOfRepairers.weHaveTooManyRepairersOverall()) {
            assignRepairersToWoundedUnits();
        }
    }

    protected void assignRepairersToWoundedUnits() {
        List<AUnit> repairable = RepairableUnits.get().list();

        for (AUnit woundedUnit : repairable) {
//            if (woundedUnit.is(AUnitType.Terran_Missile_Turret)) {

//            }

            if (ShouldNotRepairUnit.shouldNotRepairUnit(null, woundedUnit)) {
                continue;
            }

            if (OptimalNumOfRepairers.hasUnitTooManyRepairers(woundedUnit)) {
                continue;
            }

            int newRepairersNeeded = optimalNumOfRepairersFor(woundedUnit);
//            if (woundedUnit.is(AUnitType.Terran_Missile_Turret)) {

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

        if (!A.hasMinerals(5)) {
            allowedRepairers = Math.max(2, Math.min(3, Count.workers() / 8));
        }

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
                AAdvancedPainter.paintTextCentered(unit, shouldHaveThisManyRepairers + "", Color.Orange);
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
