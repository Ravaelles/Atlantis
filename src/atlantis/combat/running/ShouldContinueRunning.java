package atlantis.combat.running;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ShouldContinueRunning {
    public static boolean handleContinueRunning(AUnit unit) {
        if (unit.lastStartedRunningLessThanAgo(3)) return true;

        if (unit.isRunning()) {
            if (unit.lastStartedRunningLessThanAgo(3)) return true;

            if (unit.cooldown() <= 2) {
//                double rangeBonus = unit.isHealthy() ? 0.5 : 1.2;
                double rangeBonus = 1.1;
                if (unit.runningFrom() != null && !unit.runningFrom().canAttackTargetWithBonus(unit, rangeBonus)) {
                    return false;
                }
            }

//            double distToTargetPosition = unit.targetPosition() != null ? unit.distTo(unit.targetPosition()) : -1;

//            if (distToTargetPosition >= 2.5) return true;
//            if (distToTargetPosition >= 2 && unit.meleeEnemiesNearCount(1.7) > 0) return true;

            if (
//                unit.isMoving() && unit.lastActionLessThanAgo(15, Actions.RUN_IN_ANY_DIRECTION)
                unit.lastActionLessThanAgo(8, Actions.RUN_IN_ANY_DIRECTION)
            ) {
//                System.err.println("@ " + A.now() + " - " + unit);
                return true;
            }
        }

        return false;
    }
}
