package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class RunError extends Manager {
    public RunError(AUnit unit) {
        super(unit);
    }

    protected Manager handleErrorRun(AUnit unit, double dist) {
        if (A.isUms()) {
            if (!unit.isObserver()) {
                A.errPrintln(
                    A.now() + " ERROR_RUN for " + unit.nameWithId() + " / dist=" + A.digit(dist)
                        + " / mode=" + unit.runningManager().lastRunMode()
                        + " / nearEnemy=" + unit.nearestEnemyDist()
                );
//                A.printStackTrace("Wtf");
            }
        }

        AUnit enemy = unit.nearestEnemy();
        if (enemy != null) {
            if (unit.moveAwayFrom(enemy, 3, Actions.MOVE_AVOID)) {
                return usedManager(this, "RunErrorMoveAway");
            }

            if (unit.moveAwayFrom(enemy, 1, Actions.MOVE_AVOID)) {
                return usedManager(this, "RunErrorMoveAway");
            }
        }

        unit.addLog("RUN-ERROR");

        if (unit.noCooldown() && unit.hp() >= 80) {
//            AttackNearbyEnemies attackNearbyEnemies = new AttackNearbyEnemies(unit);
//            if (attackNearbyEnemies.handleAttackNearEnemyUnits()) {
            if ((new AttackNearbyEnemies(unit)).forceHandle() != null) {
                unit.setTooltipTactical("Cant run, fight");
                return usedManager(this, "RunErrorAttack");
            }
        }

        unit.runningManager().stopRunning();
        return null;
    }
}