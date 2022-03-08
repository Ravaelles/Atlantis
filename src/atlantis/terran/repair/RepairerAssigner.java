package atlantis.terran.repair;

import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.terran.TerranFlyingBuildingManager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class RepairerAssigner {

    protected static void assignRepairersToWoundedUnits() {
        if (!MaxRepairers.usingMoreRepairersThanAllowed() && A.hasMinerals(15)) {
            for (AUnit woundedUnit : Select.our().repairable(true).excludeTypes(AtlantisConfig.WORKER).list()) {
                if (shouldNotRepairUnit(woundedUnit)) {
                    continue;
                }

                if (MaxRepairers.tooManyRepairers(woundedUnit)) {
                    continue;
                }

                int newRepairersNeeded = MaxRepairers.optimalRepairersFor(woundedUnit);
                if (newRepairersNeeded > 0) {
                    ARepairerManager.assignRepairersToWoundedUnits(woundedUnit, newRepairersNeeded);
//                    System.out.println("Assign " + newRepairersNeeded + " repairers to " + woundedUnit.name());
                }
            }
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

    public static boolean removeExcessiveRepairersIfNeeded() {
        int allowedRepairers = MaxRepairers.MAX_REPAIRERS_AT_ONCE;

        if (MaxRepairers.usingMoreRepairersThanAllowed()) {
            for (int i = 0; i < ARepairAssignments.countTotalRepairers() - allowedRepairers; i++) {
                AUnit repairer = ARepairAssignments.getRepairers().get(ARepairAssignments.getRepairers().size() - 1);
                if (!ARepairerManager.canSafelyAbandonRepairTarget(repairer)) {
                    ARepairAssignments.removeRepairerOrProtector(repairer);
                }
//                System.err.println("Remove excessive repairer " + repairer);
            }
            return true;
        }

        return false;
    }

}
