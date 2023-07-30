package atlantis.combat.micro.avoid.buildings;

import atlantis.architecture.Manager;
import atlantis.combat.retreating.ShouldRetreat;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AvoidCombatBuildings extends Manager {

    private AvoidCombatBuildingCriticallyClose avoidCombatBuildingCriticallyClose;
    private ShouldRetreat shouldRetreat;

    public AvoidCombatBuildings(AUnit unit) {
        super(unit);
        avoidCombatBuildingCriticallyClose = new AvoidCombatBuildingCriticallyClose(unit);
        shouldRetreat = new ShouldRetreat(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    public Manager handle() {
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

        if (distTo <= criticalDist) {
//            System.err.println("@ EEEEEEEEEEEEE");
            if (thereIsNoSafetyMarginAtAll(combatBuilding)) return usedManager(avoidCombatBuildingCriticallyClose);
        }
        else if (holdPositionToShoot(combatBuilding) != null) {
            return usedManager(this);
        }
        else if (distTo <= (criticalDist + doNothingMargin)) {
            return stillSomePlaceLeft(combatBuilding);
        }
        else if (distTo < (criticalDist + doNothingMargin)) {
            return barelyAnySafetyLeft(combatBuilding);
        }
//        else if (distTo <= criticalDist && unit.isMoving() && !unit.isRunning() && unit.target() == null) {

        return null;
    }

    private Manager holdPositionToShoot(AUnit combatBuilding) {
        Selection enemiesToAttack = unit.enemiesNear().canBeAttackedBy(unit, 0);
        if (enemiesToAttack.notEmpty()) {
            if (unit.isMoving() && !unit.isRunning()) {
                unit.holdPosition("HoldToShoot");
            }
            return usedManager(this);
        }

//        if (A.seconds() % 8 <= 4) {
//            unit.holdPosition("HoldToShoot");
//            return usedManager(this);
//        }

        return null;
    }

    private boolean thereIsNoSafetyMarginAtAll(AUnit combatBuilding) {
        if (avoidCombatBuildingCriticallyClose.handle(combatBuilding) != null) {
//                System.err.println("----->");
            return true;
        }
        return false;
    }

    private Manager barelyAnySafetyLeft(AUnit combatBuilding) {
        if (unit.isRunning() || unit.isAttacking()) {
            return null;
        }

        return null;
    }

    private Manager stillSomePlaceLeft(AUnit combatBuilding) {
        APosition runFrom = combatBuilding.position();

        if (A.chance(50)) {
            runFrom = runFrom.position().randomizePosition(6);
        }

        unit.runningManager().runFrom(runFrom, 0.5, Actions.MOVE_AVOID, false);
        return usedManager(this);
    }

    private double criticalDist(AUnit combatBuilding) {
        return 9.1
            + (combatBuilding.isSunken() ? 1.6 : 0)
            + (unit.isAir() ? 0.6 : 0) + (unit.isMoving() ? 0.9 : 0)
            + unit.woundPercent() / 70;
    }

}
