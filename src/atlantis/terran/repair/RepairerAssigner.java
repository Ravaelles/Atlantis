package atlantis.terran.repair;

import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.terran.TerranFlyingBuildingScoutCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import java.util.List;

public class RepairerAssigner {

    protected static void assignRepairersToWoundedUnits() {
        List<AUnit> repairable = Select.our().repairable(true).excludeTypes(AtlantisConfig.WORKER).list();
        for (AUnit woundedUnit : repairable) {
//            if (woundedUnit.is(AUnitType.Terran_Missile_Turret)) {
//                System.out.println("Repair TURRET? ");
//            }

            if (shouldNotRepairUnit(woundedUnit)) {
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
                ARepairerManager.assignRepairersToWoundedUnits(woundedUnit, newRepairersNeeded);
//                System.err.println("Assign " + newRepairersNeeded + " repairers to " + woundedUnit.name());
            }
        }
//        }
    }

    // =========================================================

    private static boolean shouldNotRepairUnit(AUnit unit) {
        return !unit.isRepairable()
                || (unit.isAir() && unit.hp() >= 91 && unit.friendsNear().workers().notRepairing().empty())
                || unit.isScout()
                || (unit.isRunning() && unit.lastStoppedRunningLessThanAgo(30 * 2))
                || (
                    unit.isBuilding()
                        && TerranFlyingBuildingScoutCommander.isFlyingBuilding(unit)
                        && unit.lastUnderAttackLessThanAgo(30 * 6)
                )
//                || (unit.isBuilding() && !unit.isCombatBuilding() && !unit.woundPercentMin(40))
                || ARepairerManager.itIsForbiddenToRepairThisUnitNow(unit)
                || GamePhase.isEarlyGame() && (unit.isBuilding() && !unit.isCombatBuilding() && unit.enemiesNear().atLeast(2));
    }

    public static boolean removeExcessiveRepairersIfNeeded() {
        int allowedRepairers = OptimalNumOfRepairers.MAX_REPAIRERS_AT_ONCE;

        if (OptimalNumOfRepairers.weHaveTooManyRepairersOverall()) {
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

    public static int optimalNumOfRepairersFor(AUnit unit) {
        int alreadyAssigned = ARepairAssignments.countRepairersForUnit(unit) + ARepairAssignments.countProtectorsFor(unit);
        int repairersNeeded = 1;

        // === Bunker - very special case ========================================

        if (unit.isBunker()) {
            int shouldHaveThisManyRepairers = OptimalNumOfBunkerRepairers.forBunker(unit);
            if (shouldHaveThisManyRepairers > 0) {
//                System.out.println("Bunker repairers = " + shouldHaveThisManyRepairers);
                unit.setTooltipTactical(shouldHaveThisManyRepairers + " RepairNeed");
                AProtectorManager.addProtectorsForUnit(unit, shouldHaveThisManyRepairers);
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
