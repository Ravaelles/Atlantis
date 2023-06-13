package atlantis.combat.squad.positioning;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TooClustered {

    public static boolean handleTooClustered(AUnit unit) {
        if (shouldSkip(unit)) {
            return false;
        }

        Selection ourCombatUnits = Select.ourCombatUnits().inRadius(5, unit);
        AUnit nearestBuddy = ourCombatUnits.clone().nearestTo(unit);

        if (nearestBuddy != null && ourCombatUnits.size() >= 10 && nearestBuddy.distToLessThan(unit, 0.4)) {
            return unit.moveAwayFrom(nearestBuddy, 0.5, "SpreadOut", Actions.MOVE_FORMATION);
        }

        return false;
    }

    // =========================================================

    private static boolean shouldSkip(AUnit unit) {
        if (unit.isAir()) {
            return true;
        }

        if (unit.mission() != null && unit.mission().isMissionAttack()) {
            return true;
        }

        if (unit.squad().size() <= 1 || unit.isMoving()) {
            return true;
        }

        return false;
    }

}
