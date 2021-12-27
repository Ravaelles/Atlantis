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
        if (distTo <= 7.8) {
//            System.out.println(unit.nameWithId() + " " + distTo);
            return unit.runningManager().runFrom(nearest, 1);
//            return unit.runningManager().runFrom(nearest, 1, "AvoidBuilding");
        }
        else if (distTo < 8.2) {
            // Do nothing
        }
        else if (distTo < 8.8 && unit.isMoving() && !unit.isRunning() && unit.target() == null) {
            return unit.holdPosition("HoldHere");
        }

        return false;
    }

}
