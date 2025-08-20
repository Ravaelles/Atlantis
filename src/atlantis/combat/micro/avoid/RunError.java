package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.PauseAndCenter;
import bwapi.Color;

public class RunError extends Manager {
    public RunError(AUnit unit) {
        super(unit);
    }

    protected Manager handleErrorRun(AUnit unit, double dist) {
//        System.err.println("dist = " + dist);
//        System.err.println("runTo = " + unit.runningManager().runTo());
//        if (unit.runningManager().runTo() != null) System.err.println(
//            "runTo = " + unit.runningManager().runTo().isWalkable() + " / " + unit.runningManager().runTo().distToDigit(unit)
//        );

//        if (A.isUms()) {
//            if (!unit.isAir() && !"ShowBack".equals(unit.runningManager().lastRunMode())) {
//                A.errPrintln(
//                    A.now() + " ERROR_RUN for " + unit.nameWithId() + " / dist=" + A.digit(dist)
//                        + " / mode=" + unit.runningManager().lastRunMode()
//                        + " / nearEnemy=" + unit.nearestEnemyDist()
//                );
//                A.printStackTrace("Wtf");

//                if (A.isUms()) {
//                    unit.paintCircleFilled(9, Color.Red);
//                    if (unit.isDragoon()) PauseAndCenter.on(unit);
//                }
//            }
//        }

//        AUnit enemy = unit.nearestEnemy();
//        if (enemy != null) {
////            if (unit.moveAwayFrom(enemy, 3, Actions.MOVE_AVOID)) {
////                return usedManager(this, "RunErrorMoveAway");
////            }
////
////            if (unit.moveAwayFrom(enemy, 1, Actions.MOVE_AVOID)) {
////                return usedManager(this, "RunErrorMoveAway");
////            }
//
////            if (unit.moveAwayFrom(enemy, 0.2, Actions.MOVE_AVOID)) {
////                return usedManager(this, "RunErrorMoveAway");
////            }
//        }

//        unit.addLog("RUN-ERROR");
//
//        if (unit.noCooldown() && unit.hp() >= 80) {
////            AttackNearbyEnemies attackNearbyEnemies = new AttackNearbyEnemies(unit);
////            if (attackNearbyEnemies.handleAttackNearEnemyUnits()) {
//            if ((new AttackNearbyEnemies(unit)).forceHandle() != null) {
//                unit.setTooltipTactical("Cant run, fight");
//                return usedManager(this, "RunErrorAttack");
//            }
//        }

        unit.runningManager().stopRunning();
        unit.stop(null);
        return null;
    }
}