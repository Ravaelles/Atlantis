package atlantis.repair;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;

public class AUnitBeingReparedManager {

    public static boolean handleUnitBeingRepaired(AUnit unit) {
        if (!unit.isWounded()) {
            return false;
        }

        AUnit repairer = ARepairAssignments.getRepairerAssignedForUnit(unit);
        if (repairer == null) {
            return false;
        }

        if (unit.isRunning()) {
            return false;
        }

        // Ignore going closer to repairer if unit is still relatively healthy
        if (unit.getHPPercent() > 50) {
            return false;
        }

        // =========================================================

        double distanceToRepairer = repairer.distanceTo(unit);

        // Go to repairer if he's close
        if (distanceToRepairer > 2) {
            unit.setTooltip("Move to repair");
            unit.move(repairer.getPosition(), UnitActions.MOVE_TO_REPAIR);
            return true;
        }

        unit.setTooltip("Be repaired");
        unit.holdPosition();
        return true;
    }

}