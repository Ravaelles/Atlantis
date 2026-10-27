package atlantis.combat.strategy;

/**
 * NEXT #186: hold EARLY instead of kiting to the nominal standoff.
 *
 * <p>The nominal dragoon range (4.0 tiles) is not the range at which a shot
 * actually lands while both sides move: braking glides us, the order takes
 * latency frames, windup runs after firing, and the chaser keeps closing
 * through all of it. The range that matters is the EFFECTIVE one (R*):
 * foe reach, plus what the chaser covers during our windup, minus our
 * braking glide. This class answers the race given R*: if the weapon is
 * ready when the enemy reaches R* (with a hold margin for order-issue
 * slop), stand and wait (no wasted ground); otherwise keep kiting.
 *
 * <p>Pure time-vs-time, no game reads: gap and range in pixels, times in
 * frames. Head-on scope - the caller passes the closing speed along the
 * gap axis (a committed chaser runs it at ~full speed straight at us).
 * A runner (closing <= 0) never holds: the chase rule owns runners, and
 * DuelKite never yields ground to them either.
 */
public final class ProtossAttackHoldToShoot {

    private ProtossAttackHoldToShoot() {
    }

    /**
     * @param gapPx             current gap to the chaser, pixels
     * @param effectiveRangePx  gap at which our damage lands (R* <= nominal range)
     * @param cooldownLeft      weapon cooldown frames remaining
     * @param latencyFrames     order-latency frames before an issued order acts
     * @param closingPxPerFrame chaser's closing speed along the gap axis, px/frame
     * @param holdMarginFrames  extra frames of arrival lead required to stand (order-issue slop)
     * @return true when the weapon is ready on enemy arrival: stand and wait
     */
    public static boolean shouldHoldEarly(
        double gapPx, double effectiveRangePx, int cooldownLeft, int latencyFrames,
        double closingPxPerFrame, int holdMarginFrames
    ) {
        if (closingPxPerFrame <= 0) return false;

        double reachIn = (gapPx - effectiveRangePx) / closingPxPerFrame;
        int readyIn = cooldownLeft + latencyFrames;

        if (reachIn <= 0) return readyIn <= 0;

        return reachIn >= readyIn + holdMarginFrames;
    }

    /**
     * The effective range R*: foe reach, plus what the chaser covers
     * during our windup, minus our braking glide (we keep sliding after
     * cutting orders, which opens the gap). Floored at zero: braking
     * longer than the whole exchange means the stop is free.
     */
    public static double effectiveRange(
        double foeReachPx, double foeSpeedPxPerFrame, int windupFrames, double brakingPx
    ) {
        return Math.max(0, foeReachPx + foeSpeedPxPerFrame * windupFrames - brakingPx);
    }
}
