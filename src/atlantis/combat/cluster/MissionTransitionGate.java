package atlantis.combat.cluster;

/**
 * The mediator for mission changes (owner order: a superior concept
 * deciding whether defend&lt;-&gt;attack transitions may happen, not
 * logic inside the mission itself).
 *
 * <p>Rules, in frames (30 per second): after a withdrawal
 * (attack-ward to defend-ward) attacks hold fire for 5 seconds (150f);
 * withdrawals themselves re-fire every 2 seconds (60f). Same-to-same
 * and defend-to-defend transitions are always allowed - this gate
 * owns only the defend&lt;-&gt;attack axis.</p>
 *
 * <p>Pure except for the one timestamp it keeps: {@link #decide} is the
 * table-pinned verdict (snapshot in, verdict out), {@link #allows} is
 * the wiring seam that stamps withdrawals. Attack means
 * {@code ATTACK_ENEMY_BASE} only - staging (STAGE_MIDDLE) counts
 * defend-ward, so falling back to the middle is a spaced withdrawal
 * and pushing out of it honours the hold-fire.</p>
 */
public final class MissionTransitionGate {

    /** 5 seconds of hold-fire after a withdrawal. */
    public static final int NO_ATTACK_FRAMES_AFTER_WITHDRAW = 150;

    /** Withdrawals re-fire every 2 seconds (flap guard, not a lockout). */
    public static final int WITHDRAW_EVERY_FRAMES = 60;

    public enum Verdict {
        ALLOW,
        HOLD_FIRE_POST_WITHDRAW,
        WITHDRAW_TOO_SOON
    }

    private static int lastWithdrawFrame = Integer.MIN_VALUE / 2;

    private MissionTransitionGate() {
    }

    public static void clearForTests() {
        lastWithdrawFrame = Integer.MIN_VALUE / 2;
    }

    private static boolean isAttack(ArmyMission mission) {
        return mission == ArmyMission.ATTACK_ENEMY_BASE;
    }

    /**
     * Pure verdict: may the mission move from {@code from} to {@code to}
     * at frame {@code now}, given the last allowed withdrawal happened
     * at {@code lastWithdraw}. Null {@code from} (undecided) always
     * allows - the first decision sticks.
     */
    public static Verdict decide(
        ArmyMission from, ArmyMission to, int now, int lastWithdraw
    ) {
        if (from == null || from == to) return Verdict.ALLOW;

        boolean leavingAttack = isAttack(from) && !isAttack(to);
        boolean enteringAttack = !isAttack(from) && isAttack(to);

        if (leavingAttack) {
            return now - lastWithdraw >= WITHDRAW_EVERY_FRAMES
                ? Verdict.ALLOW
                : Verdict.WITHDRAW_TOO_SOON;
        }

        if (enteringAttack) {
            return now - lastWithdraw >= NO_ATTACK_FRAMES_AFTER_WITHDRAW
                ? Verdict.ALLOW
                : Verdict.HOLD_FIRE_POST_WITHDRAW;
        }

        return Verdict.ALLOW;
    }

    /**
     * Wiring seam: the verdict plus the stamp. Only an allowed
     * withdrawal moves the timestamp - a forbidden one never happened.
     */
    public static boolean allows(ArmyMission current, ArmyMission proposed, int now) {
        Verdict verdict = decide(current, proposed, now, lastWithdrawFrame);

        if (verdict == Verdict.ALLOW
            && current != null && isAttack(current) && !isAttack(proposed)) {
            lastWithdrawFrame = now;
        }

        return verdict == Verdict.ALLOW;
    }

    /**
     * Human sentence for the verdict, with the deciding numbers -
     * the reason lives with the decider, never scattered at callers.
     */
    public static String reason(
        Verdict verdict, ArmyMission from, ArmyMission to, int now, int lastWithdraw
    ) {
        switch (verdict) {
            case HOLD_FIRE_POST_WITHDRAW:
                return "attack held " + (now - lastWithdraw) + "f after withdraw (need "
                    + NO_ATTACK_FRAMES_AFTER_WITHDRAW + "f)";
            case WITHDRAW_TOO_SOON:
                return "withdraw " + (now - lastWithdraw) + "f after previous (need "
                    + WITHDRAW_EVERY_FRAMES + "f)";
            default:
                return from + "->" + to + " allowed";
        }
    }
}
