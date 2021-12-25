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

        double distTo = nearest.distTo(unit);
        if (distTo < 7.7) {
            return unit.runningManager().runFrom(nearest, 1);
//            return unit.runningManager().runFrom(nearest, 1, "AvoidBuilding");
        }
        else if (distTo < 8.0 && unit.isMoving() && !unit.isRunning()) {
            return unit.holdPosition("HoldHere");
        }

        return false;
    }

}
