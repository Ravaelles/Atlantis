package atlantis.combat.micro.avoid.buildings;

import atlantis.combat.retreating.ShouldRetreat;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.managers.Manager;
import atlantis.units.select.Selection;

public class AvoidCombatBuildings extends Manager {

    private AvoidCombatBuildingCriticallyClose avoidCombatBuildingCriticallyClose;
    private ShouldRetreat shouldRetreat;

    public AvoidCombatBuildings(AUnit unit) {
        super(unit);
        avoidCombatBuildingCriticallyClose = new AvoidCombatBuildingCriticallyClose(unit);
        shouldRetreat = new ShouldRetreat(unit);
    }

    public  Manager handle() {
        if (unit.isMissionDefendOrSparta()) {
            return null;
        }

        Selection combatBuildings = EnemyUnits.discovered().combatBuildings(false);

        AUnit combatBuilding = combatBuildings.inRadius(12, unit).canAttack(unit, 6).nearestTo(unit);
        if (combatBuilding == null) {
//            APainter.paintCircleFilled(8, Color.Green);
            return null;
        }

//        APainter.paintCircleFilled(8, Color.Red);
//        System.err.println("@ C = " + ShouldRetreat.shouldRetreat(unit));
        if (
                !unit.isAir()
                && unit.friendsInRadiusCount(3) >= 6
                && unit.friendsInRadiusCount(5) >= 8
                && !shouldRetreat.shouldRetreat(unit)
                && combatBuildings.combatBuildings(false).inRadius(10, unit).notEmpty()
        ) {
//            unit.setTooltip("@ D YOLO " + unit);
            return null;
        }

//        double criticalDist = 9.8 + (unit.isAir() ? 2.5 : 0);
        double criticalDist = criticalDist(combatBuilding);
        double distTo = combatBuilding.distTo(unit);

        double doNothingMargin = 0.3;
        if (distTo <= (criticalDist + doNothingMargin)) {
            unit.runningManager().runFrom(combatBuilding, 0.5, Actions.MOVE_AVOID, false);
            return usingManager(this);
        }
        else if (distTo < (criticalDist + doNothingMargin)) {
            // Do nothing
        }
//        else if (distTo <= criticalDist && unit.isMoving() && !unit.isRunning() && unit.target() == null) {
        else if (distTo <= criticalDist) {
//            System.err.println("@ EEEEEEEEEEEEE");
            if (avoidCombatBuildingCriticallyClose.handle(combatBuilding) != null) {
//                System.err.println("----->");
                return lastManager();
            }
        }

        return null;
    }

    private  double criticalDist(AUnit combatBuilding) {
        return 9.1
            + (combatBuilding.isSunken() ? 1.6 : 0)
            + (unit.isAir() ? 0.9 : 0) + (unit.isMoving() ? 0.9 : 0)
            + unit.woundPercent() / 200.0;
    }

}
