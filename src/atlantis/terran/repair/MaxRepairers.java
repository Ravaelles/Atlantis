package atlantis.terran.repair;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class MaxRepairers {

    public static final int MAX_REPAIRERS_AT_ONCE = 8;

    // =========================================================

    public static boolean tooManyRepairers(AUnit unit) {
        return ARepairAssignments.countRepairersForUnit(unit) >= optimalRepairersFor(unit);
    }

    public static int optimalRepairersFor(AUnit unit) {
        int alreadyAssigned = ARepairAssignments.countRepairersForUnit(unit) + ARepairAssignments.countProtectorsFor(unit);
        int repairersNeeded = 1;

        // === Bunker - very special case ========================================

        if (unit.isBunker()) {
            int shouldHaveThisManyRepairers = ARepairCommander.defineOptimalRepairersForBunker(unit);
            if (shouldHaveThisManyRepairers > 0) {
                unit.setTooltipTactical(shouldHaveThisManyRepairers + " RepairNeed");
                AProtectorManager.assignProtectorsFor(unit, shouldHaveThisManyRepairers - repairersNeeded);
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

    public static boolean usingMoreRepairersThanAllowed() {
        return ARepairAssignments.countTotalRepairers() > MAX_REPAIRERS_AT_ONCE;
    }

}
