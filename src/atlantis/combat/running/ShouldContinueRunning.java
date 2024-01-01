package atlantis.combat.running;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class ShouldContinueRunning {
    public static boolean handleContinueRunning(AUnit unit) {
        if (unit.isRunning()) {
            if (unit.lastStartedRunningLessThanAgo(3)) return truth(unit);
            if (unit.lastActionLessThanAgo(5, Actions.MOVE_AVOID)) return truth(unit);

            if (unit.cooldown() <= 2) {
//                double rangeBonus = unit.isHealthy() ? 0.5 : 1.2;
                double rangeBonus = 1.1;
                if (unit.runningFrom() != null && unit.runningFrom().canAttackTargetWithBonus(unit, rangeBonus)) {
//                    System.err.println("@ " + A.now() + " - stahp");
                    return false;
                }
            }

//            double distToTargetPosition = unit.targetPosition() != null ? unit.distTo(unit.targetPosition()) : -1;

//            if (distToTargetPosition >= 2.5) return truth(unit);
//            if (distToTargetPosition >= 2 && unit.meleeEnemiesNearCount(1.7) > 0) return truth(unit);

            if (
//                unit.isMoving() && unit.lastActionLessThanAgo(15, Actions.RUN_IN_ANY_DIRECTION)
                unit.lastActionLessThanAgo(8, Actions.RUN_IN_ANY_DIRECTION)
            ) {
//                System.err.println("@ " + A.now() + " - " + unit);
                return truth(unit);
            }
        }

        return false;
    }

    private static boolean truth(AUnit unit) {
//        System.err.println("@ " + A.now() + " - CONTINUE");
        unit.paintCircleFilled(5, Color.White);
        return true;
    }
}
