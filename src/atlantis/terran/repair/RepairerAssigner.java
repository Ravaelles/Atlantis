package atlantis.terran.repair;

import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.terran.TerranFlyingBuildingScoutManager;
import atlantis.units.AUnit;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class RepairerAssigner {

    protected static void assignRepairersToWoundedUnits() {
//        if (!MaxRepairers.usingMoreRepairersThanAllowed()) {
        for (AUnit woundedUnit : Select.our().repairable(true).excludeTypes(AtlantisConfig.WORKER).list()) {
            if (shouldNotRepairUnit(woundedUnit)) {
                continue;
            }

            if (MaxRepairers.tooManyRepairers(woundedUnit)) {
                continue;
            }

            int newRepairersNeeded = optimalRepairersFor(woundedUnit);
            if (newRepairersNeeded > 0) {
                ARepairerManager.assignRepairersToWoundedUnits(woundedUnit, newRepairersNeeded);
//                    System.out.println("Assign " + newRepairersNeeded + " repairers to " + woundedUnit.name());
            }
        }
//        }
    }

    // =========================================================

    private static boolean shouldNotRepairUnit(AUnit unit) {
        return !unit.isRepairable()
                || (unit.isAir() && unit.hp() >= 51)
                || unit.isScout()
                || (unit.isRunning() && unit.lastStoppedRunningLessThanAgo(30 * 5))
                || (unit.isBuilding() && TerranFlyingBuildingScoutManager.isFlyingBuilding(unit) && unit.lastUnderAttackLessThanAgo(30 * 15))
                || (unit.isBuilding() && !unit.isCombatBuilding() && !unit.woundPercent(40))
                || ARepairerManager.itIsForbiddenToRepairThisUnitNow(unit)
                || GamePhase.isEarlyGame() && (unit.isBuilding() && !unit.isCombatBuilding() && unit.enemiesNear().atLeast(2));
    }

    public static boolean removeExcessiveRepairersIfNeeded() {
        int allowedRepairers = MaxRepairers.MAX_REPAIRERS_AT_ONCE;

        if (MaxRepairers.usingMoreRepairersThanAllowed()) {
            for (int i = 0; i < ARepairAssignments.countTotalRepairers() - allowedRepairers; i++) {
                AUnit repairer = ARepairAssignments.getRepairers().get(ARepairAssignments.getRepairers().size() - 1);
                if (!ARepairerManager.canSafelyAbandonUnitToBeRepaired(repairer)) {
                    ARepairAssignments.removeRepairer(repairer);
                }
//                System.err.println("Remove excessive repairer " + repairer);
            }
            return true;
        }

        return false;
    }

    public static int optimalRepairersFor(AUnit unit) {
        int alreadyAssigned = ARepairAssignments.countRepairersForUnit(unit) + ARepairAssignments.countProtectorsFor(unit);
        int repairersNeeded = 1;

        // === Bunker - very special case ========================================

        if (unit.isBunker()) {
            int shouldHaveThisManyRepairers = MaxRepairers.optimalRepairersForBunker(unit);
            if (shouldHaveThisManyRepairers > 0) {
                unit.setTooltipTactical(shouldHaveThisManyRepairers + " RepairNeed");
                AProtectorManager.assignProtectorsFor(unit, shouldHaveThisManyRepairers);
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
}
