package atlantis.combat.cluster;

/**
 * The mission instantiated for the frame (owner 2026-11-09, mission +
 * task direction): the mission names the cluster's job, the task is
 * the standing order - where the cluster belongs (anchor kind),
 * which posture it holds, whether FLEE is allowed at all and, when
 * it is, which line it rallies to (null: the retreat ladder owns
 * routing). Pure table over the mission, no live reads - the
 * Vocabulary half of MISSION-DRIVEN-COMMAND.md §2, pinned by
 * ClusterTaskTest.
 *
 * <p>Shadow stage: nothing reads a task yet. Enforcement wires one
 * mission's task at a time with its own game proof (same discipline
 * as the HOLD_* slices); until then posture, tactic and strategy
 * keep deciding on their own.</p>
 */
public final class ClusterTask {

    private final ArmyMission mission;
    private final TaskAnchor anchor;
    private final ClusterMission.Posture posture;
    private final boolean fleeAllowed;
    private final TaskAnchor fleeRally;

    private ClusterTask(
        ArmyMission mission, TaskAnchor anchor, ClusterMission.Posture posture,
        boolean fleeAllowed, TaskAnchor fleeRally
    ) {
        this.mission = mission;
        this.anchor = anchor;
        this.posture = posture;
        this.fleeAllowed = fleeAllowed;
        this.fleeRally = fleeRally;
    }

    public ArmyMission mission() {
        return mission;
    }

    public TaskAnchor anchor() {
        return anchor;
    }

    public ClusterMission.Posture posture() {
        return posture;
    }

    public boolean fleeAllowed() {
        return fleeAllowed;
    }

    public TaskAnchor fleeRally() {
        return fleeRally;
    }

    /**
     * One row per mission, first match wins (there is exactly one row
     * per mission - the table IS the decision). Mirrors
     * MISSION-DRIVEN-COMMAND.md §2: HOLD_MAIN never flees (breach
     * overrun aside, owned by ClusterBreach), choke holds rally to
     * their own line, the middle stages rally at the line, the attack
     * leaves routing to the retreat ladder.
     */
    public static ClusterTask forMission(ArmyMission mission) {
        if (mission == null) return holdMain();

        switch (mission) {
            case HOLD_MAIN:
                return holdMain();
            case HOLD_MAIN_CHOKE:
                return new ClusterTask(
                    mission, TaskAnchor.MAIN_CHOKE, ClusterMission.Posture.DEFEND,
                    true, TaskAnchor.MAIN_CHOKE);
            case HOLD_NATURAL_CHOKE:
                return new ClusterTask(
                    mission, TaskAnchor.NATURAL_CHOKE, ClusterMission.Posture.DEFEND,
                    true, TaskAnchor.NATURAL_CHOKE);
            case STAGE_MIDDLE:
                return new ClusterTask(
                    mission, TaskAnchor.MIDDLE_CHOKE, ClusterMission.Posture.DEFEND,
                    true, TaskAnchor.MIDDLE_CHOKE);
            case HOLD_MIDDLE:
                return new ClusterTask(
                    mission, TaskAnchor.MIDDLE_CHOKE, ClusterMission.Posture.DEFEND,
                    true, TaskAnchor.MIDDLE_CHOKE);
            case ATTACK_ENEMY_BASE:
                return new ClusterTask(
                    mission, TaskAnchor.ENEMY_BASE, ClusterMission.Posture.ATTACK,
                    true, null);
            default:
                return holdMain();
        }
    }

    private static ClusterTask holdMain() {
        return new ClusterTask(
            ArmyMission.HOLD_MAIN, TaskAnchor.MAIN_BASE, ClusterMission.Posture.DEFEND,
            false, null);
    }
}
