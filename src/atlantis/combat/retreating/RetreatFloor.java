package atlantis.combat.retreating;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.focus.MiddleFocusChoke;
import atlantis.combat.strategy.ChokeStandoff;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Retreat floor (owner order 2026-10-25: "if we need to retreat, never
 * further than MiddleFocusChoke"): a losing fight stops at the
 * middle-choke line instead of running all the way back to the main.
 * Standing too deep is what lost games - the army fell back, piled onto
 * the base and could not attack again.
 *
 * <p>The line is the same RADIUS + SPARE_TILES standoff the formation
 * rule answers ({@link atlantis.combat.strategy.ChokeStandoff}), so a
 * retreat ends on the ground the army would hold anyway. The floor only
 * stops a retreat that would CROSS it: an army already inside keeps its
 * own rally (yanking a small line out through its own base would be the
 * opposite of the order). A breach never reaches this path -
 * {@code ClusterTactic} forces FIGHT while raiders are in the base.</p>
 */
public final class RetreatFloor {

    private RetreatFloor() {
    }

    /**
     * The destination a retreat may fall back to: unchanged when it is
     * already in front of (or on) the middle line, the line itself when
     * the retreat would go deeper toward the main. Null-safe, and it
     * never throws - an unreadable world keeps today's rally.
     */
    public static HasPosition clamp(HasPosition dest) {
        if (dest == null) return null;

        try {
            AUnit main = Select.main();
            if (main == null || main.position() == null) return dest;

            AChoke middle = MiddleFocusChoke.get();
            if (middle == null || middle.center() == null) return dest;

            APosition line = ChokeStandoff.holdPoint(middle, main.position());
            if (line == null) return dest;

            // Only a retreat that would CROSS the line is stopped: an
            // army already inside its own base keeps today's rally
            // (yanking a small line out through the base would be the
            // opposite of the order).
            HasPosition center = Alpha.get().center();
            if (center == null) return dest;
            if (center.distTo(main) < line.distTo(main)) return dest;

            return dest.distTo(main) < line.distTo(main) ? line : dest;
        }
        catch (RuntimeException ignored) {
            return dest;
        }
    }
}
