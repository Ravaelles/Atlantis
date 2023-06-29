package atlantis.combat.micro.avoid.buildings;

import atlantis.combat.retreating.ShouldRetreat;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AvoidCombatBuildings {

    public static boolean update(AUnit unit) {
        if (unit.isMissionDefendOrSparta()) {
            return false;
        }

//        if (enemyCombatBuildings == null) {
        Selection combatBuildings = EnemyUnits.discovered().combatBuildings(false);
//        }

        AUnit combatBuilding = combatBuildings.nearestTo(unit);
        if (combatBuilding == null) {
//            APainter.paintCircleFilled(unit, 8, Color.Green);
            return false;
        }

//        System.err.println("@ C = " + ShouldRetreat.shouldRetreat(unit));
        if (
                unit.friendsInRadiusCount(3) >= 4
                && unit.friendsInRadiusCount(5) >= 7
                && !ShouldRetreat.shouldRetreat(unit)
                && combatBuildings.combatBuildings(false).inRadius(10, unit).notEmpty()
        ) {
//            unit.setTooltip("@ D YOLO " + unit);
            return false;
        }

//        double criticalDist = 9.8 + (unit.isAir() ? 2.5 : 0);
        double criticalDist = criticalDist(unit, combatBuilding);
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
//            System.err.println("@ EEEEEEEEEEEEE");
            if (AvoidCombatBuildingCriticallyClose.handle(unit, combatBuilding)) {
//                System.err.println("----->");
                return true;
            }
        }

        return false;
    }

    private static double criticalDist(AUnit unit, AUnit combatBuilding) {
        return 8.3
            + (combatBuilding.isSunken() ? 1.6 : 0)
            + (unit.isAir() ? 0.9 : 0) + (unit.isMoving() ? 0.8 : 0)
            + unit.woundPercent() / 200.0;
    }

}
