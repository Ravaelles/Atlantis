package atlantis.battle;

/**
 * One gate for the whole cluster: fight or flee. The evaluation itself
 * stays with the combat evaluator (leader's hedged ratio); this class
 * owns only the threshold and the retreat latch.
 *
 * <p>Clock-free by construction: the frame arrives as a parameter, so
 * the latch pins in a unit test with fake frames. Threshold 1.0 both
 * ways is deliberately crude (B-1 owns the scale question) - the latch,
 * not finesse, stops oscillation. Both numbers become knobs when the
 * tuning battery exists.</p>
 */
public class FightOrFlight {

    /** Eval at or above this means fight. Below means flee. */
    public static final double FIGHT_EVAL = 1.0;

    /** A retreat lasts at least this long before re-evaluation. */
    public static final int RETREAT_LATCH_FRAMES = 240;

    public enum Tactic {
        FIGHT,
        FLEE
    }

    public static Tactic decide(double eval, int now, int fleeStartedAt) {
        if (fleeStartedAt >= 0 && now - fleeStartedAt < RETREAT_LATCH_FRAMES) {
            return Tactic.FLEE;
        }
        return eval >= FIGHT_EVAL ? Tactic.FIGHT : Tactic.FLEE;
    }
}
