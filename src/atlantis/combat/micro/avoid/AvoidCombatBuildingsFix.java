package atlantis.combat.micro.avoid;

import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;

public class AvoidCombatBuildingsFix {

    public static boolean handle(AUnit unit, Units enemyCombatBuildings) {
        AUnit nearest = Select.from(enemyCombatBuildings).nearestTo(unit);
        if (nearest == null) {
            return false;
        }

        double baseDist = 7.8 + (unit.isAir() ? 2.5 : 0);
        double distTo = nearest.distTo(unit);
        if (distTo <= baseDist) {
            return unit.runningManager().runFrom(nearest, 1);
        }
        else if (distTo < (baseDist + 0.4)) {
            // Do nothing
        }
        else if (distTo < (baseDist + 1) && unit.isMoving() && !unit.isRunning() && unit.target() == null) {
            return unit.holdPosition("HoldHere", false);
        }

        return false;
    }

}
