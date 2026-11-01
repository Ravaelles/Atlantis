package atlantis.combat.retreating;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.focus.MiddleFocusChoke;
import atlantis.combat.retreating.RetreatLadder.Rung;
import atlantis.combat.strategy.ChokeStandoff;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Retreat floor (owner orders 2026-10-25 "never further than
 * MiddleFocusChoke" and 2026-10-26 the ladder that grades it): a losing
 * fight stops on the choke line it was holding instead of running all
 * the way back to the main. Standing too deep is what lost games - the
 * army fell back, piled onto the base and could not attack again.
 *
 * <p>The rung comes from {@link RetreatLadder} (the ground the army
 * last fought on): beyond the middle choke line it falls back to that
 * line, losing even there drops it to the natural (main) choke line,
 * and that is the floor - the Nexus itself is never a destination. Both
 * lines are the same RADIUS + SPARE_TILES standoff the formation rule
 * answers ({@link ChokeStandoff}), so a retreat ends on ground the army
 * would hold anyway.</p>
 *
 * <p>Two guards keep the floor from drafting orders of its own: an army
 * still inside the natural choke line keeps its own rally (base defense
 * is a different rule - a breach forces FIGHT), and a rung standing
 * AHEAD of the army is never marched onto: the army holds where it is
 * rather than advancing while it flees. Both are the owner's order read
 * literally - stop going deeper, not "advance to the line".</p>
 */
public final class RetreatFloor {

    /**
     * Depth answer for a world where the path names no middle choke.
     */
    private static final double NO_MIDDLE_LINE = -1;

    private static Rung lastTwinnedRung = null;
    private static int lastTwinFrame = -1000000;

    private RetreatFloor() {
    }

    public static void clearForTests() {
        lastTwinnedRung = null;
        lastTwinFrame = -1000000;
    }

    /**
     * The destination a retreat may fall back to: unchanged when it is
     * already in front of the rung, the rung itself when the retreat
     * would cross it, the army's own ground when the rung sits ahead of
     * it. Null-safe, and it never throws - an unreadable world keeps
     * today's rally.
     */
    public static HasPosition clamp(HasPosition dest) {
        if (dest == null) return null;

        try {
            AUnit main = Select.main();
            if (main == null || main.position() == null) return dest;

            HasPosition center = Alpha.get().center();
            if (center == null) return dest;

            AChoke natural = Chokes.mainChoke();
            APosition naturalLine = natural == null ? null : ChokeStandoff.holdPoint(natural, main.position());
            if (naturalLine == null) return dest;

            double armyDepth = center.distTo(main.position());
            double naturalDepth = naturalLine.distTo(main.position());

            // Inside its own choke line the army keeps today's rally:
            // base defense is its own rule (breach = FIGHT, breach
            // rally), and yanking it out to a rung it already stands
            // behind would be an order forward, not a retreat.
            if (armyDepth <= naturalDepth) return dest;

            AChoke middle = MiddleFocusChoke.get();
            APosition middleLine = middle == null ? null : ChokeStandoff.holdPoint(middle, main.position());
            double middleDepth = middleLine == null ? NO_MIDDLE_LINE : middleLine.distTo(main.position());

            Rung rung = RetreatLadder.rungFor(RetreatLadder.lastFightDepth(), naturalDepth, middleDepth);
            HasPosition target = rung == Rung.MIDDLE && middleLine != null ? middleLine : naturalLine;

            // A rung ahead of the army is held, never marched onto (a
            // fleeing army must not be ordered back into the enemy).
            if (target.distTo(main.position()) > armyDepth) {
                twinFloor(rung, center, "ahead-hold");

                return center;
            }

            // Only a retreat that would CROSS the rung is stopped: a
            // rally already in front of it keeps its own destination.

            if (dest.distTo(main.position()) < target.distTo(main.position())) {
                twinFloor(rung, target, "clamped");

                return target;
            }

            return dest;
        } catch (RuntimeException ignored) {
            return dest;
        }
    }

    /**
     * Slice 4: the floor reports its overrides upward - a clamped rally
     * is a re-decide (the retreat wanted deeper), not a silent sideways
     * stop. Change-gated per rung with a 300-frame heartbeat, same shape
     * as the mission twins.
     */
    private static void twinFloor(Rung rung, HasPosition at, String why) {
        try {
            if (rung == null || at == null || at.position() == null) return;

            int now = atlantis.game.A.now();
            boolean transition = lastTwinnedRung != rung;
            if (!transition && now - lastTwinFrame < 300) return;

            lastTwinnedRung = rung;
            lastTwinFrame = now;
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.RETREAT_FLOOR)
                .param("rung", rung.name())
                .param("tx", at.tx())
                .param("ty", at.ty())
                .param("why", why)
                .param("transition", transition)
                .log(now, atlantis.game.A.minSec());
        } catch (Exception ignored) {
        }
    }
}
