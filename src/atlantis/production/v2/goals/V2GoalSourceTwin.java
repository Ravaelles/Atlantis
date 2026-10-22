package atlantis.production.v2.goals;

import atlantis.production.v2.ProductionGoal;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

import java.util.List;

/**
 * Authorship witness for V2 supply-provider demand (WO-144 open question):
 * when a duplicate Pylon pair lands, the two plan-time rows name which
 * list re-fired - a BO-row re-fire, pipeline-legal warping overlap, or a
 * door hole each read differently off the source column.
 *
 * <p>Pure over the goal lists: the engine calls it with the frame, tests
 * call it with numbers. Only supply-provider goals are witnessed - the
 * pair question is supply-specific, and every other goal would be noise.</p>
 */
public final class V2GoalSourceTwin {

    private V2GoalSourceTwin() {
    }

    public static void emit(int frame, List<ProductionGoal> goals, String providerId) {
        if (goals == null || providerId == null) return;

        for (ProductionGoal goal : goals) {
            if (goal == null || goal.item() == null) continue;
            if (!providerId.equals(goal.item().id())) continue;
            EventLog.key(LogKey.V2_GOAL_SOURCE)
                .param("source", goal.source())
                .param("item", goal.item().id())
                .param("priority", goal.priority())
                .param("count", goal.count())
                .log(frame, frameToClock(frame));
        }
    }

    private static String frameToClock(int frame) {
        int seconds = frame / 24;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
