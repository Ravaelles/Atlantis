package atlantis.repair;

import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

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
            ARepairAssignments.removeRepairerOrProtector(repairer);
            return false;
        }

        // Ignore going closer to repairer if unit is still relatively healthy
        if (unitBeingRepared.hpPercent() > 50 && distanceToRepairer >= 3) {
            return false;
        }

        // =========================================================


        // Go to repairer if he's close
        if (distanceToRepairer > 2) {
            unitBeingRepared.move(repairer.getPosition(), UnitActions.MOVE_TO_REPAIR, "To repairer");
            return true;
        }

        if (distanceToRepairer <= 1) {
            unitBeingRepared.holdPosition("Be repaired");
            return true;
        }

        return false;
    }

}