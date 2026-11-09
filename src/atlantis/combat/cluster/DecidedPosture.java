package atlantis.combat.cluster;

/**
 * The frame's decided posture, noted once per frame by the commander
 * loop (P2: decisions flow down, never re-derived where the frame
 * already decided). Readers that used to re-derive the posture
 * (headcount checks) or read the global mirror (which only follows
 * the decision) take it here instead; paths outside handle() - unit
 * tests, non-combat callers - see null and keep their old fallback.
 */
public final class DecidedPosture {

    private static ClusterMission.Posture noted = null;

    private DecidedPosture() {
    }

    public static void note(ClusterMission.Posture posture) {
        noted = posture;
    }

    public static ClusterMission.Posture currentOrNull() {
        return noted;
    }

    public static void clearForTests() {
        noted = null;
    }
}
