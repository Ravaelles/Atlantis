package atlantis.terran.repair;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ABunkerProtectorManager {

    public static boolean updateProtector(AUnit protector) {
        AUnit unit = ARepairAssignments.getUnitToProtectFor(protector);
        if (unit != null && unit.isAlive()) {

            // WOUNDED
            if (unit.isWounded() && A.hasMinerals(5)) {
                protector.repair(unit, "Protect" + unit.name(), true);
                return true;
            }

            // Bunker fully HEALTHY
            else {
                double distanceToUnit = unit.distTo(protector);
                if (distanceToUnit > 1 && !protector.isMoving()) {
                    protector.move(unit.position(), Actions.MOVE_REPAIR, "ProtectNearer" + unit.name(), true);
                    return true;
                }
                else {
                    protector.setTooltipTactical("Protecting" + unit.name());
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
