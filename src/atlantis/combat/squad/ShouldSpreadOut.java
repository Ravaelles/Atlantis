package atlantis.combat.squad;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ShouldSpreadOut {

    public static boolean handleSpreadOut(AUnit unit) {
        if (unit.mission() != null && unit.mission().isMissionAttack()) {
            return false;
        }

        if (unit.squad().size() <= 1 || unit.isMoving()) {
            return false;
        }

        Selection ourCombatUnits = Select.ourCombatUnits().inRadius(5, unit);
        AUnit nearestBuddy = ourCombatUnits.clone().nearestTo(unit);

        if (nearestBuddy != null && nearestBuddy.distToLessThan(unit, 0.5)) {
            return unit.moveAwayFrom(nearestBuddy, 0.5, "SpreadOut", Actions.MOVE_FORMATION);
        }

        return false;
    }

}
