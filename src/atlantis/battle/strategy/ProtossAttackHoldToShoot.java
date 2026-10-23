package atlantis.battle.strategy;

/**
 * NEXT #186: hold EARLY instead of kiting to the nominal standoff.
 *
 * <p>The nominal dragoon range (4.0 tiles) is not the range at which a shot
 * actually lands while both sides move: braking glides us, the order takes
 * latency frames, windup runs after firing, and the chaser keeps closing
 * through all of it. The range that matters is the EFFECTIVE one (R*):
 * the gap at which the enemy must be for damage to land, computed by a
 * later slice from windup/braking/latency. This class answers the race
 * given R*, nothing more: if the weapon is ready when the enemy reaches
 * R*, stand and wait (no wasted ground); otherwise keep kiting (buy time).
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
     * @param gapPx            current gap to the chaser, pixels
     * @param effectiveRangePx gap at which our damage lands (R* <= nominal range)
     * @param cooldownLeft     weapon cooldown frames remaining
     * @param latencyFrames    order-latency frames before an issued order acts
     * @param closingPxPerFrame chaser's closing speed along the gap axis, px/frame
     * @return true when the weapon is ready on enemy arrival: stand and wait
     */
    public static boolean shouldHoldEarly(
        double gapPx, double effectiveRangePx, int cooldownLeft, int latencyFrames, double closingPxPerFrame
    ) {
        if (closingPxPerFrame <= 0) return false;

        double reachIn = (gapPx - effectiveRangePx) / closingPxPerFrame;
        int readyIn = cooldownLeft + latencyFrames;

        if (reachIn <= 0) return readyIn <= 0;

        return reachIn >= readyIn;
    }
}
