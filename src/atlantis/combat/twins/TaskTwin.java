package atlantis.combat.twins;

import atlantis.combat.cluster.ArmyMission;
import atlantis.combat.cluster.ClusterTask;
import atlantis.game.A;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * The task twin (mission + task direction, owner 2026-11-09): what the
 * enforced mission computed as this frame's standing order, with the
 * fields that will one day steer enforcement (anchor kind, posture,
 * flee permission, flee rally). Transitions always row, steady tasks
 * heartbeat - twin before gate, so a game log shows the task alive
 * and stable before any branch reads it.
 */
public final class TaskTwin {

    public static final int HEARTBEAT_FRAMES = 300;

    private static String lastMission;
    private static int lastTwinFrame = -1000000;

    private TaskTwin() {
    }

    public static void clearForTests() {
        lastMission = null;
        lastTwinFrame = -1000000;
    }

    public static void sample(ArmyMission mission) {
        try {
            if (mission == null) return;

            ClusterTask task = ClusterTask.forMission(mission);
            int now = A.now();
            boolean transition = !mission.name().equals(lastMission);
            if (!transition && now - lastTwinFrame < HEARTBEAT_FRAMES) return;

            lastMission = mission.name();
            lastTwinFrame = now;

            EventLog.key(LogKey.TASK)
                .param("mission", mission.name())
                .param("anchor", task.anchor() == null ? "-" : task.anchor().name())
                .param("posture", task.posture() == null ? "-" : task.posture().name())
                .param("fleeAllowed", task.fleeAllowed())
                .param("fleeRally", task.fleeRally() == null ? "-" : task.fleeRally().name())
                .param("transition", transition)
                .log(now, A.minSec());
        } catch (Exception ignored) {
        }
    }
}
