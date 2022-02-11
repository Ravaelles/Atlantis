package atlantis.terran.repair;

import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.terran.TerranFlyingBuildingManager;
import atlantis.units.AUnit;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class RepairerAssigner {

    public static final int MAX_REPAIRERS_AT_ONCE = 8;

    protected static void assignRepairersToWoundedUnits() {
        if (!usingMoreRepairersThanAllowed() && A.hasMinerals(15)) {
            for (AUnit woundedUnit : Select.our().repairable(true).excludeTypes(AtlantisConfig.WORKER).list()) {
                if (shouldNotRepairUnit(woundedUnit)) {
                    continue;
                }

                if (tooManyRepairs(woundedUnit)) {
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

    private static boolean shouldNotRepairUnit(AUnit unit) {
        return !unit.isRepairable()
                || (unit.isAir() && unit.hp() >= 51)
                || unit.isScout()
                || (unit.isRunning() && unit.lastStoppedRunningLessThanAgo(30 * 5))
                || (unit.isBuilding() && TerranFlyingBuildingManager.isFlyingBuilding(unit) && unit.lastUnderAttackLessThanAgo(30 * 15))
                || (unit.isBuilding() && !unit.isCombatBuilding() && !unit.woundPercent(40))
                || ARepairerManager.itIsForbiddenToRepairThisUnitNow(unit)
                || GamePhase.isEarlyGame() && (unit.isBuilding() && !unit.isCombatBuilding() && unit.enemiesNear().atLeast(2));
    }

    private static boolean tooManyRepairs(AUnit unit) {
        return ARepairAssignments.countRepairersForUnit(unit) >= optimalRepairersFor(unit);
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

    private static boolean removeExcessiveRepairersIfNeeded() {
        int allowedRepairers = A.hasMinerals(5) ? MAX_REPAIRERS_AT_ONCE : 0;

        if (usingMoreRepairersThanAllowed()) {
            for (int i = 0; i < ARepairAssignments.countTotalRepairers() - allowedRepairers; i++) {
                AUnit repairer = ARepairAssignments.getRepairers().get(ARepairAssignments.getRepairers().size() - 1);
//                System.err.println("Remove excessive repairer " + repairer);
                ARepairAssignments.removeRepairerOrProtector(repairer);
            }
            return true;
        }

        return false;
    }

    public static boolean usingMoreRepairersThanAllowed() {
        return ARepairAssignments.countTotalRepairers() > MAX_REPAIRERS_AT_ONCE;
    }
}
