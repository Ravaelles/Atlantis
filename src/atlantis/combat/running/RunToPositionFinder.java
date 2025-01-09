package atlantis.combat.running;

import atlantis.combat.running.any_direction.RunInAnyDirection;
import atlantis.combat.running.show_back.RunShowingBackToEnemy;
import atlantis.combat.running.to_building.ShouldRunTowardsBase;
import atlantis.combat.running.to_building.ShouldRunTowardsCB;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Action;
import atlantis.units.actions.Actions;

public class RunToPositionFinder {

    protected final ARunningManager running;
    protected final RunInAnyDirection runInAnyDirection = new RunInAnyDirection(this);
    protected final RunShowingBackToEnemy runShowBackToEnemy;

    public RunToPositionFinder(atlantis.combat.running.ARunningManager runningManager) {
        this.running = runningManager;
        this.runShowBackToEnemy = new RunShowingBackToEnemy(runningManager);
    }

    /**
     * Running behavior which will make unit run straight away from the enemy.
     */
    protected HasPosition findBestPositionToRun(HasPosition runAwayFrom, double dist, Action action) {
        AUnit unit = running.unit;

        HasPosition currentRunTo = unit.runningManager().runTo();
        if (currentRunTo != null && currentRunTo.distTo(unit) >= 1.4 && unit.lastRunningPositionChangeAgo() <= 14) {
//            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - CONT RUN TO");
            return currentRunTo;
        }

        if (
            !unit.isFlying()
                && !unit.isScout()
                && !action.equals(Actions.MOVE_DANCE_AWAY)
        ) {
            // Run to BUNKER
            if (ShouldRunTowardsCB.check(unit, runAwayFrom)) {
                running._lastRunMode = "TowardsCB";
                AUnit position = ShouldRunTowardsCB.position();
                if (position != null) {
//                    unit.paintCircleFilled(8, Color.Teal);
                    return running.setRunTo(position);
                }
            }

            // === Run directly to base ========================

            if (ShouldRunTowardsBase.check(unit, runAwayFrom)) {
                AUnit position = ShouldRunTowardsBase.position();
                if (position != null) {
                    running._lastRunMode = "TowardsBase";
//                    unit.paintCircleFilled(3, Color.Yellow);
                    return running.setRunTo(position);
                }
            }
        }

        // === Run directly away from the enemy ========================

        if (running.showBackToEnemy.shouldRunByShowingBackToEnemy()) {
//            System.err.println("show back to enemy");
            if (runShowBackToEnemy.findPositionForShowingBackToEnemy(runAwayFrom)) {
//                System.err.println("show back to enemy OK");
                running._lastRunMode = "ShowBack";
//                Color color = Color.Blue;
//                HasPosition runTo = unit.runningManager().runTo;
//                APainter.paintLine(unit, runTo, color);
//                APainter.paintLine(
//                    unit.translateByPixels(0, 1),
//                    runTo.translateByPixels(0, 1),
//                    color
//                );

                return running.runTo();
            }
        }

        // === Run as far from enemy as possible =====================

        running._lastRunMode = "AnyDirection";
        return running.setRunTo(runInAnyDirection.runInAnyDirection(runAwayFrom));
    }

    // =========================================================

    public ARunningManager running() {
        return running;
    }
}
