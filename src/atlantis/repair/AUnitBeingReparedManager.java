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

        if (unitBeingRepared.isRunning()) {
            return false;
        }

        // Ignore going closer to repairer if unit is still relatively healthy
        if (unitBeingRepared.getHPPercent() > 50) {
            return false;
        }

        // =========================================================

        double distanceToRepairer = repairer.distanceTo(unitBeingRepared);

        // Go to repairer if he's close
        if (distanceToRepairer > 2) {
            unitBeingRepared.move(repairer.getPosition(), UnitActions.MOVE_TO_REPAIR, "To repairer");
            return true;
        }

        unitBeingRepared.holdPosition("Being repaired");
        return true;
    }

}