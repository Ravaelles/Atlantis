package atlantis.production.demand;

import atlantis.production.demand.ProductionGoal;
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

    private static String lastDetectionRow;
    private static int lastDetectionFrame = -1000000;

    /**
     * Authorship witness for the hidden-unit crisis response (owner
     * #223): while the snapshot flag holds, which detection goals the
     * goal layer emits - Robo/Observatory/Observer/Forge/cannons - with
     * their sources. Transitions always row, steady threat heartbeats,
     * so a long lurker siege reads as one row per change instead of one
     * per frame. Silence here while the chain is expected means the
     * goals never left the layer (vs the scheduler/dispatcher dropping
     * them downstream, which the dispatch twins would name).
     */
    public static void emitDetection(int frame, List<ProductionGoal> goals, boolean threatFlag) {
        if (!threatFlag || goals == null) {
            lastDetectionRow = null;
            return;
        }

        StringBuilder row = new StringBuilder();
        for (ProductionGoal goal : goals) {
            if (goal == null || goal.item() == null) continue;
            String id = goal.item().id();
            if (!isDetectionItem(id)) continue;
            row.append(id).append(':').append(goal.source()).append(':').append(goal.count()).append(';');
        }
        if (row.length() == 0) {
            lastDetectionRow = null;
            return;
        }

        String key = row.toString();
        boolean transition = !key.equals(lastDetectionRow);
        if (!transition && frame - lastDetectionFrame < 300) return;

        lastDetectionRow = key;
        lastDetectionFrame = frame;
        EventLog.key(LogKey.V2_DETECTION_GOALS)
            .param("goals", key)
            .param("transition", transition)
            .log(frame, frameToClock(frame));
    }

    private static boolean isDetectionItem(String id) {
        return id != null && (id.contains("Robotics") || id.contains("Observator")
            || id.contains("Observer") || id.contains("Forge") || id.contains("Cannon"));
    }

    public static void clearForTests() {
        lastDetectionRow = null;
        lastDetectionFrame = -1000000;
    }

    private static String frameToClock(int frame) {
        int seconds = frame / 24;
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
