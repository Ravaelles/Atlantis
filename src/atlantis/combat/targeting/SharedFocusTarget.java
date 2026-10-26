package atlantis.combat.targeting;

import atlantis.combat.formation.FocusFire;
import atlantis.units.AUnit;

/**
 * Shared focus (ladder rule (5)): the weakest damageable enemy inside the
 * squad envelope, whether or not it is inside our own weapon range. Focus,
 * not chase: the pick is a target to share, the walk there belongs to the
 * advance, never to this gate. Split out of {@code AcquireTarget} as a
 * named rule, ordered visibly in {@link TargetLadder}: one behavior, one
 * gate, the coordination preference itself stays a removal candidate
 * (NEXT #184) until a sim no-regression proof promotes it.
 */
public final class SharedFocusTarget {

    /** The squad envelope both focus rules read (was the ladder's literal). */
    public static final double SHARED_FOCUS_TILES = 9;

    private SharedFocusTarget() {
    }

    public static AUnit pick(AUnit unit) {
        if (unit == null) return null;

        try {
            return FocusFire.sharedTarget(unit, SHARED_FOCUS_TILES);
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
