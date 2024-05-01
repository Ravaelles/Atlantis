package atlantis.combat.running;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.PauseAndCenter;
import bwapi.Color;

public class ShouldContinueRunning {
    public static boolean handleContinueRunning(AUnit unit) {
//        if (unit.isRunning()) {
//            unit.paintCircleFilled(18, Color.Blue);
//        }
//        else {
//            unit.paintCircle(19, Color.Grey);
//            unit.paintCircle(18, Color.Grey);
//            unit.paintCircle(13, Color.Grey);
//            unit.paintCircle(12, Color.Grey);
//        }

//        if (unit.isDragoon()) return false;

        if (unit.isRunning()) {
            APosition targetPosition = unit.targetPosition();
            if (targetPosition == null || targetPosition.distTo(unit) <= 0.75) return false;

            if (justStartedRunning(unit)) return truth(unit);

            if (
//                unit.isMoving() && unit.lastActionLessThanAgo(15, Actions.RUN_IN_ANY_DIRECTION)
                unit.lastActionLessThanAgo(8, Actions.RUN_IN_ANY_DIRECTION)
            ) {
//                System.err.println("@ " + A.now() + " - " + unit);
                return truth(unit);
            }

            if (unit.cooldown() <= 2) {
//                double rangeBonus = unit.isHealthy() ? 0.5 : 1.2;
                double rangeBonus = unitInDifficultSituation(unit) ? 2.9 : 1.1;
                if (
                    unit.runningFrom() != null
                        && unit.runningFrom().canAttackTargetWithBonus(unit, rangeBonus)
                ) {
//                    System.err.println("@ " + A.now() + " - stahp");
                    return false;
                }
            }

//            double distToTargetPosition = unit.targetPosition() != null ? unit.distTo(unit.targetPosition()) : -1;

//            if (distToTargetPosition >= 2.5) return truth(unit);
//            if (distToTargetPosition >= 2 && unit.meleeEnemiesNearCount(1.7) > 0) return truth(unit);
        }

        return false;
    }

    private static boolean unitInDifficultSituation(AUnit unit) {
        if (unit.hp() <= 17) return true;

        return !unit.hasMedicInHealRange()
            && unit.runningFrom() != null
            && unit.runningFrom().isFacing(unit);
    }

    private static boolean justStartedRunning(AUnit unit) {
        int MAX_FRAMES_AGO = justStartedRunningFramesThreshold(unit);

//        if (!unit.isMoving() && !unit.isRunning()) return false;

        if (unit.lastStartedRunningLessThanAgo(MAX_FRAMES_AGO)) return true;
        if (unit.lastActionLessThanAgo(MAX_FRAMES_AGO, Actions.MOVE_AVOID)) return true;

        return false;
    }

    private static int justStartedRunningFramesThreshold(AUnit unit) {
        if (unit.isDragoon()) return 25;

        return 15;
    }

    private static boolean truth(AUnit unit) {
//        System.err.println("@ " + A.now() + " - CONTINUE RUN " + unit.idWithHash());
//        unit.paintCircleFilled(5, Color.White);
//        unit.paintCircle(9, Color.White);
//        unit.paintCircle(10, Color.White);
        return true;
    }
}
