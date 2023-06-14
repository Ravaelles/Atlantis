package atlantis.combat.micro.avoid.buildings;

import atlantis.combat.retreating.ShouldRetreat;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class AvoidCombatBuildingsFix {

    public static boolean handle(AUnit unit, Units enemyCombatBuildings) {
        if (unit.isMissionDefendOrSparta()) {
            return false;
        }

        AUnit combatBuilding = Select.from(enemyCombatBuildings).nearestTo(unit);
        if (combatBuilding == null) {
            return false;
        }

        if (
                unit.isMissionAttack()
                && !ShouldRetreat.shouldRetreat(unit)
                && enemyCombatBuildings.selection().combatBuildings(false).inRadius(10, unit).notEmpty()
        ) {
            return false;
        }

//        double criticalDist = 9.8 + (unit.isAir() ? 2.5 : 0);
        double criticalDist = 7.5 + (unit.isAir() ? 0.5 : 0) + (unit.isMoving() ? 0.5 : 0)
            + unit.woundPercent() / 200.0;
        double distTo = combatBuilding.distTo(unit);

        double doNothingMargin = 0.3;
        if (distTo <= (criticalDist + doNothingMargin)) {
            return unit.runningManager().runFrom(combatBuilding, 0.5, Actions.MOVE_AVOID);
        }
        else if (distTo < (criticalDist + doNothingMargin)) {
            // Do nothing
        }
//        else if (distTo <= criticalDist && unit.isMoving() && !unit.isRunning() && unit.target() == null) {
        else if (distTo <= criticalDist) {
            if (AvoidCombatBuildingCriticallyClose.handle(unit, combatBuilding)) {
                return true;
            }
        }

        return false;
    }

}
