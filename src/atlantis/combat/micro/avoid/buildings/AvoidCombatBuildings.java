package atlantis.combat.micro.avoid.buildings;

import atlantis.combat.retreating.ShouldRetreat;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class AvoidCombatBuildings {

    public static boolean update(AUnit unit, Units enemyCombatBuildings) {
        if (unit.isMissionDefendOrSparta()) {
            return false;
        }

        if (enemyCombatBuildings == null) {
            enemyCombatBuildings = EnemyUnits.discovered().combatBuildings(false).units();
        }

        AUnit combatBuilding = Select.from(enemyCombatBuildings).nearestTo(unit);
        if (combatBuilding == null) {
            return false;
        }

//        System.err.println("@ C = " + ShouldRetreat.shouldRetreat(unit));
        if (
                unit.friendsInRadiusCount(3) >= 2
                && unit.friendsInRadiusCount(5) >= 7
                && !ShouldRetreat.shouldRetreat(unit)
                && enemyCombatBuildings.selection().combatBuildings(false).inRadius(10, unit).notEmpty()
        ) {
//            System.err.println("@ D YOLO");
            return false;
        }

//        double criticalDist = 9.8 + (unit.isAir() ? 2.5 : 0);
        double criticalDist = criticalDist(unit);
        double distTo = combatBuilding.distTo(unit);

        double doNothingMargin = 0.3;
        if (distTo <= (criticalDist + doNothingMargin)) {
            return unit.runningManager().runFrom(combatBuilding, 0.5, Actions.MOVE_AVOID, false);
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

    private static double criticalDist(AUnit unit) {
        return 7.5 + (unit.isAir() ? 0.9 : 0) + (unit.isMoving() ? 0.5 : 0)
            + unit.woundPercent() / 200.0;
    }

}
