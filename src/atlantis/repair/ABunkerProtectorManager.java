package atlantis.repair;

import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;

public class ABunkerProtectorManager {

    public static boolean updateProtector(AUnit protector) {
        AUnit unit = ARepairAssignments.getUnitToProtectFor(protector);
        if (unit != null && unit.isAlive()) {

            // WOUNDED
            if (unit.woundPercent() >= 1 && A.hasMinerals(5)) {
                protector.repair(unit, "Protect " + unit.name(), true);
                return true;
            }

            // Bunker fully HEALTHY
            else {
                double distanceToUnit = unit.distTo(protector);
                if (distanceToUnit > 1 && !protector.isMoving()) {
                    protector.move(unit.position(), UnitActions.MOVE_TO_REPAIR, "Go to " + unit.name(), true);
                    return true;
                }
                else {
                    protector.setTooltipTactical("Protect " + unit.name());
                }
            }
        }
        else {
            protector.setTooltipTactical("Null bunker");
            ARepairAssignments.removeRepairerOrProtector(protector);
            return true;
        }

        return ARepairerManager.handleIdleRepairer(protector);
    }
}
