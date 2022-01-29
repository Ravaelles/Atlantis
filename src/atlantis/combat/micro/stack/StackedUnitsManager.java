package atlantis.combat.micro.stack;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class StackedUnitsManager {

    public static boolean dontStackTooMuch(AUnit unit, double minDist, boolean onlyOfTheSameType) {
        AUnit nearest = (onlyOfTheSameType ? Select.ourOfType(unit.type()) : Select.ourRealUnits())
                .exclude(unit).inRadius(minDist, unit).nearestTo(unit);

        if (nearest != null) {
            unit.moveAwayFrom(nearest, minDist / 2, "Stacked", Actions.MOVE_FORMATION);
            return true;
        }

        return false;
    }

}
