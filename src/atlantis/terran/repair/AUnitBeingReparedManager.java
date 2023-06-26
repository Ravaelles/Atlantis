package atlantis.terran.repair;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AUnitBeingReparedManager {

    public static boolean handleUnitShouldBeRepaired(AUnit unit) {
        if (!unit.isWounded()) {
            return false;
        }

        if (true) return true;

        AUnit repairer = ARepairAssignments.getClosestRepairerAssignedTo(unit);
        if (repairer == null) {
            return false;
        }

        double distanceToRepairer = repairer.distTo(unit);
        if (unit.isRunning()) {
            return false;
        }

        if (!unit.isWounded()) {
            ARepairAssignments.removeRepairer(repairer);
            return false;
        }

        // Air units should be repaired thoroughly
        if (unit.isAir() && unit.isBeingRepaired()) {
            if (!unit.isRunning() && unit.isMoving()) {
                unit.setTooltip("HoldTheFuckDown");
                return true;
            }
        }

        // Ignore going closer to repairer if unit is still relatively healthy
        if (!unit.isAir() && unit.hpPercent() > 80 && distanceToRepairer >= 3) {
            return false;
        }

        // =========================================================

        if (unit.nearestEnemyDist() <= 1.7) {
            unit.setTooltip("DontRepairEnemy");
            return false;
        }

        // Go to repairer if he's close
        if (distanceToRepairer > 1) {
            if (unit.move(repairer.position(), Actions.MOVE_REPAIR, "To repairer", false)) {
                return true;
            }
        }

        if (distanceToRepairer <= 1) {
            unit.holdPosition("Be repaired", false);
            return true;
        }

        return false;
    }

}