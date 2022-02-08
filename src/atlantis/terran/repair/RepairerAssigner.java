package atlantis.terran.repair;

import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.map.scout.AScoutManager;
import atlantis.terran.TerranFlyingBuildingManager;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class RepairerAssigner {

    public static final int MAX_REPAIRERS_AT_ONCE = 8;

    protected static void assignRepairersToWoundedUnits() {
        if (!hasMoreRepairersThanAllowed() && A.hasMinerals(15)) {
            for (AUnit woundedUnit : Select.our().repairable(true).excludeTypes(AtlantisConfig.WORKER).list()) {
                if (shouldNotRepairUnit(woundedUnit)) {
                    continue;
                }

                int newRepairersNeeded = optimalRepairersFor(woundedUnit);
                if (newRepairersNeeded > 0) {
                    ARepairerManager.assignRepairersToWoundedUnits(woundedUnit, newRepairersNeeded);
//                    System.out.println("Assign " + newRepairersNeeded + " repairers to " + woundedUnit.name());
                }
            }
        }

        if (removeExcessiveRepairersIfNeeded()) {
            return;
        }
    }

    // =========================================================

    private static boolean shouldNotRepairUnit(AUnit target) {
        return !target.isRepairable()
                || (target.isAir() && target.hp() >= 51)
                || AScoutManager.isScout(target)
                || (target.isRunning() && target.lastStoppedRunningLessThanAgo(30 * 5))
                || (target.isBuilding() && TerranFlyingBuildingManager.isFlyingBuilding(target) && target.lastUnderAttackLessThanAgo(30 * 15))
                || (target.isBuilding() && !target.isCombatBuilding() && !target.woundPercent(40))
                || ARepairerManager.itIsForbiddenToRepairThisUnitNow(target);
    }

    private static int optimalRepairersFor(AUnit unit) {
        int alreadyAssigned = ARepairAssignments.countRepairersForUnit(unit) + ARepairAssignments.countProtectorsFor(unit);
        int repairersNeeded = 1;

        // === Bunker - very special case ========================================

        if (unit.isBunker()) {
            int shouldHaveThisManyRepairers = ARepairCommander.defineOptimalRepairersForBunker(unit);
            unit.setTooltipTactical(shouldHaveThisManyRepairers + "RepNeed");
            ARepairCommander.assignProtectorsFor(unit, shouldHaveThisManyRepairers - repairersNeeded);
        }
        else if (unit.isMissileTurret()) {
            int enemies = Select.enemyCombatUnits().air().inRadius(10, unit).count();

            if (Have.main() && Select.main().distToLessThan(unit, 14)) {
                return A.inRange(3, enemies, 5);
            }

            return A.inRange(2, (int) (enemies / 1.5), 5);
        }
        else if (unit.isTank()) {
            return 2;
        }

        return Math.max(0, repairersNeeded - alreadyAssigned);
    }

    private static boolean removeExcessiveRepairersIfNeeded() {
        int allowedRepairers = A.hasMinerals(5) ? MAX_REPAIRERS_AT_ONCE : 0;

        if (hasMoreRepairersThanAllowed()) {
            for (int i = 0; i < ARepairAssignments.countTotalRepairers() - allowedRepairers; i++) {
                AUnit repairer = ARepairAssignments.getRepairers().get(ARepairAssignments.getRepairers().size() - 1);
//                System.err.println("Remove excessive repairer " + repairer);
                ARepairAssignments.removeRepairerOrProtector(repairer);
            }
            return true;
        }

        return false;
    }

    public static boolean hasMoreRepairersThanAllowed() {
        return ARepairAssignments.countTotalRepairers() > MAX_REPAIRERS_AT_ONCE;
    }
}
