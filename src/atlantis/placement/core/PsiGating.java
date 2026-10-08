package atlantis.placement.core;

import java.util.List;

/**
 * Protoss Psi gating: whether a tile is powered now, and when a tile will be
 * powered (`_AI/redesign/03_PLACEMENT.md` §2 Step 5.1, §5.4 S3).
 *
 * <p>
 * A Protoss building that {@code needsPower()} cannot start until a completed
 * Pylon covers it. The catalogue therefore records, per location,
 * {@code framesUntilPowered}: {@code 0} already powered, a completion frame
 * when a
 * Pylon that will cover it is under construction, {@code -1} when nothing can
 * ever
 * power it.
 * </p>
 *
 * <p>
 * Pure domain: the two questions ("is this tile powered", "when does this
 * Pylon complete") come through a port, so the gating logic is unit-testable
 * and
 * the engine adapter is the only place that reads the game.
 * </p>
 */
public final class PsiGating {

    /**
     * The engine answers "is this tile powered" and "when will this Pylon finish"
     * - the two facts gating needs.
     */
    public interface PowerSource {
        /** True when a completed Pylon covers this tile right now. */
        boolean isPowered(int tx, int ty);

        /**
         * Completion frames (relative to now) of Pylons under construction that
         * will cover this tile once done, earliest first. Empty when none will.
         */
        List<Integer> incomingPowerFrames(int tx, int ty);

        /**
         * Could a Pylon we may place cover this tile - i.e. is there a free spot
         * near enough that a new Pylon would reach it? False when the tile is
         * walled off from every placeable spot, which is what makes it a hard
         * refuse rather than "go build a Pylon".
         */
        boolean canBeCoveredByNewPylon(int tx, int ty);
    }

    /** Tiles a Pylon powers from its own tile (BWAPI's Pylon power radius). */
    public static final int PYLON_POWER_RADIUS_TILES = 6;

    private final PowerSource power;

    public PsiGating(PowerSource power) {
        this.power = power;
    }

    /**
     * {@code 0} when powered now; the earliest completion frame of an incoming
     * Pylon otherwise; {@code -1} when nothing will ever power this tile.
     *
     * <p>
     * This is the "approximate" value the plan warns about (§4.6): a Pylon that is
     * itself delayed shifts every tile it powers. Keeping the estimate here, in one
     * function, is what keeps the catalogue deterministic and the imprecision
     * isolated.
     * </p>
     */
    public int framesUntilPowered(int tx, int ty) {
        if (power.isPowered(tx, ty))
            return 0;

        List<Integer> incoming = power.incomingPowerFrames(tx, ty);
        if (incoming == null || incoming.isEmpty())
            return -1;

        int earliest = Integer.MAX_VALUE;
        for (Integer frame : incoming) {
            if (frame != null && frame < earliest)
                earliest = frame;
        }
        return earliest == Integer.MAX_VALUE ? -1 : earliest;
    }

    /**
     * Should this building wait for power? A building that does not
     * {@code needsPower} never does; one whose tile is powered or has incoming
     * power does not either - the scheduler shifts it to the powered frame. Only a
     * tile nothing can power is a hard no.
     */
    public boolean canEverBePowered(int tx, int ty) {
        return framesUntilPowered(tx, ty) >= 0;
    }

    /**
     * What the planner should do about a candidate's power (`§2` Step 5.1 rule 3,
     * S3's pull-forward):
     * </p>
     */
    public enum PowerVerdict {
        /** Powered now, or by a Pylon that will finish first anyway. */
        ACCEPT,
        /** Not powered, but a Pylon placed here would fix it - build one. */
        NEEDS_NEW_PYLON,
        /** Nothing can ever power this tile; refuse it. */
        REFUSE
    }

    /**
     * The verdict for a power-needing building at this tile.
     *
     * <p>
     * A tile a <b>new</b> Pylon could power answers {@link PowerVerdict#NEEDS_NEW_PYLON}
     * rather than {@code REFUSE} - that is the difference between "this spot is no
     * good" and "this spot is good, go get a Pylon". Stardust models the same
     * choice as "queue a new Pylon only if it can beat the current best by the
     * builder-travel buffer"; the buffer is a policy constant, so it stays here as
     * a parameter rather than being baked into the verdict.
     * </p>
     */
    public PowerVerdict verdictFor(int tx, int ty) {
        if (power.isPowered(tx, ty)) return PowerVerdict.ACCEPT;

        List<Integer> incoming = power.incomingPowerFrames(tx, ty);
        if (incoming != null && !incoming.isEmpty()) return PowerVerdict.ACCEPT;

        return powerCouldReach(tx, ty) ? PowerVerdict.NEEDS_NEW_PYLON : PowerVerdict.REFUSE;
    }

    /**
     * Could any Pylon we are allowed to place reach this tile? Answered by the
     * source, because "where may a Pylon go" is a map question (it needs the
     * catalogue), not a power question.
     */
    private boolean powerCouldReach(int tx, int ty) {
        return power.canBeCoveredByNewPylon(tx, ty);
    }
}
