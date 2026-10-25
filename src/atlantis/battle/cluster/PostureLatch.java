package atlantis.battle.cluster;

/**
 * Posture stability (NEXT #197): a flip holds long enough to march
 * somewhere before the next count flicker flips it back, or the anchor
 * yo-yos between home and the enemy base every few frames. Both old
 * mission changers had this (attack-commit 5 s strong / 8 s weak,
 * defend-commit 4 s); the numbers ride verbatim, the weak/strong line
 * is the existing headcount, no new threshold.
 *
 * <p>Breach never latches: raiders in the base answer immediately, the
 * same way the old force-defend bypassed the cooldown. A backwards
 * clock (new game/world) releases the latch instead of holding
 * forever.</p>
 */
public final class PostureLatch {

    private PostureLatch() {
    }

    /** A fresh ATTACK holds this long before a retreat is allowed. */
    public static final int ATTACK_HOLD_FRAMES = 96;

    /** A fresh DEFEND holds this long for a big army before marching. */
    public static final int DEFEND_HOLD_STRONG_FRAMES = 120;

    /** Small armies flicker most: hold their DEFEND longer. */
    public static final int DEFEND_HOLD_WEAK_FRAMES = 192;

    private static ClusterMission.Posture last = null;
    private static int changedAt = 0;

    /**
     * The latched posture for this frame. Pure except the recorded flip:
     * same inputs, same answer, which is what makes the windows pinnable
     * without a game.
     */
    public static ClusterMission.Posture hold(
        ClusterMission.Posture fresh, int ours, int now, ClusterMission.Posture recorded, int recordedAt
    ) {
        if (recorded == null || now < recordedAt) return fresh;

        if (fresh == recorded) return fresh;

        boolean strong = ours >= ClusterMission.ATTACK_ARMY_SIZE;
        int hold = fresh == ClusterMission.Posture.ATTACK
            ? (strong ? DEFEND_HOLD_STRONG_FRAMES : DEFEND_HOLD_WEAK_FRAMES)
            : ATTACK_HOLD_FRAMES;

        return now - recordedAt < hold ? recorded : fresh;
    }

    /** Frame-entry point: holds the flip, records it only when it lands. */
    public static ClusterMission.Posture hold(ClusterMission.Posture fresh, int ours, int now) {
        if (last == null || now < changedAt) {
            last = fresh;
            changedAt = now;

            return fresh;
        }

        ClusterMission.Posture answer = hold(fresh, ours, now, last, changedAt);
        if (fresh != last && answer == fresh) {
            last = fresh;
            changedAt = now;
        }

        return answer;
    }

    public static void resetForTests() {
        last = null;
        changedAt = 0;
    }
}
