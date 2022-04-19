package atlantis.combat.micro.avoid;

import atlantis.combat.retreating.RetreatManager;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class AvoidCombatBuildingsFix {

    public static boolean handle(AUnit unit, Units enemyCombatBuildings) {
        if (unit.isMissionDefendOrSparta()) {
            return false;
        }

        AUnit nearest = Select.from(enemyCombatBuildings).nearestTo(unit);
        if (nearest == null) {
            return false;
        }

        if (
                unit.isMissionAttack()
                && !RetreatManager.shouldRetreat(unit)
                && enemyCombatBuildings.selection().combatBuildings(false).inRadius(10, unit).notEmpty()
        ) {
            return false;
        }

        double baseDist = 9.8 + (unit.isAir() ? 2.5 : 0);
        double distTo = nearest.distTo(unit);
        if (distTo <= baseDist) {
            return unit.runningManager().runFrom(nearest, 1, Actions.MOVE_AVOID);
        }
        else if (distTo < (baseDist + 0.4)) {
            // Do nothing
        }
        else if (distTo < (baseDist + 1) && unit.isMoving() && !unit.isRunning() && unit.target() == null) {
            if (unit.isMoving()) {
                unit.holdPosition("HoldHere", false);
            }
            return true;
        }

        return false;
    }

}
