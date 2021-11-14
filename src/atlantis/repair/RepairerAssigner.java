package atlantis.repair;

import atlantis.AtlantisConfig;
import atlantis.buildings.managers.TerranFlyingBuildingManager;
import atlantis.scout.AScoutManager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class RepairerAssigner {

    public static final int MAX_REPAIRERS_AT_ONCE = 6;

    protected static void assignRepairersToWoundedUnits() {
//        if (ARepairAssignments.repairersToUnit.keySet().size() >= Count.workers() * MAX_REPAIRERS)
        if (removeExcessiveRepairersIfNeeded()) {
            return;
        }

        for (AUnit woundedUnit : Select.our().repairable(true).excludeTypes(AtlantisConfig.WORKER).listUnits()) {
            if (!woundedUnit.isRepairable()) {
                continue;
            }

            if (removeExcessiveRepairersIfNeeded()) {
                return;
            }

            // Some units shouldn't be repaired
            if (
                    AScoutManager.isScout(woundedUnit)
                    || TerranFlyingBuildingManager.isFlyingBuilding(woundedUnit)
                    || (woundedUnit.isRunning() && woundedUnit.lastStoppedRunningLessThanAgo(30 * 5))
            ) {
                continue;
            }

            // =========================================================

            int newRepairersNeeded = newRepairersNeededFor(woundedUnit);

            // === Repair ordinary unit =================================

            if (newRepairersNeeded > 0) {
                ARepairerManager.assignRepairersToWoundedUnits(woundedUnit, newRepairersNeeded);
            }
        }
    }

    // =========================================================

    private static int newRepairersNeededFor(AUnit unit) {
        int alreadyAssigned = ARepairAssignments.countRepairersForUnit(unit) + ARepairAssignments.countProtectorsFor(unit);
        int repairersNeeded = 1;

        // === Bunker - very special case ========================================

        if (unit.isBunker()) {
            int shouldHaveThisManyRepairers = ARepairCommander.defineOptimalRepairersForBunker(unit);
            unit.setTooltip(shouldHaveThisManyRepairers + "RepNeed");
            ARepairCommander.assignProtectorsFor(unit, shouldHaveThisManyRepairers - repairersNeeded);
        }
        else if (unit.isMissileTurret()) {
            return 2;
        }
        else if (unit.isTank()) {
            return 2;
        }

        return Math.max(0, repairersNeeded - alreadyAssigned);
    }

    private static boolean removeExcessiveRepairersIfNeeded() {
//        System.out.println("REPR = " + ARepairAssignments.countTotalRepairers() + " // " + MAX_REPAIRERS);
        if (ARepairAssignments.countTotalRepairers() >= MAX_REPAIRERS_AT_ONCE) {
            for (int i = 0; i < ARepairAssignments.countTotalRepairers() - MAX_REPAIRERS_AT_ONCE; i++) {
                AUnit repairer = ARepairAssignments.getRepairers().get(ARepairAssignments.getRepairers().size() - 1);
//                System.out.println("Remove repairer " + repairer);
                ARepairAssignments.removeRepairerOrProtector(repairer);
            }
            return true;
        }

        return false;
    }

    public static boolean hasMoreRepairersThanAllowed() {
        return ARepairAssignments.countTotalRepairers() < MAX_REPAIRERS_AT_ONCE;
    }
}
