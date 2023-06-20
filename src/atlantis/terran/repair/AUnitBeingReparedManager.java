package atlantis.terran.repair;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class AUnitBeingReparedManager {

    public static boolean handleUnitBeingRepaired(AUnit unitBeingRepared) {
        if (!unitBeingRepared.isWounded()) {
            return false;
        }

        AUnit repairer = ARepairAssignments.getClosestRepairerAssignedTo(unitBeingRepared);
        if (repairer == null) {
            return false;
        }

        double distanceToRepairer = repairer.distTo(unitBeingRepared);
        if (unitBeingRepared.isRunning()) {
            return false;
        }

        if (!unitBeingRepared.isWounded()) {
            ARepairAssignments.removeRepairer(repairer);
            return false;
        }

        // Ignore going closer to repairer if unit is still relatively healthy
        if (unitBeingRepared.hpPercent() > 50 && distanceToRepairer >= 3) {
            return false;
        }

        // =========================================================

        if (unitBeingRepared.nearestEnemyDist() <= 1.7) {
            unitBeingRepared.setTooltip("DontRepairEnemy");
            return false;
        }

        // Go to repairer if he's close
        if (distanceToRepairer > 1) {
            unitBeingRepared.move(repairer.position(), Actions.MOVE_REPAIR, "To repairer", false);
            return true;
        }

        if (distanceToRepairer <= 1) {
            unitBeingRepared.holdPosition("Be repaired", false);
            return true;
        }

        return false;
    }

}