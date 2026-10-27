package atlantis.combat.retreating;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Retreat ladder (owner order 2026-10-26: "keep a history of the places
 * we fought around - if we fought beyond MiddleFocusChoke, go back to
 * it; if we lose even there, fall back to the natural choke, but never
 * further, never to the main"). The ladder answers WHICH rung a losing
 * fight drops to, from the depth of the ground the army last engaged
 * on: beyond the middle choke line it is the middle rung, otherwise the
 * natural (main) choke, which is also the floor - there is no rung
 * below it.
 *
 * <p>The memory is written by the fight funnel
 * ({@code ArmyContact.noteFight}) on engaged frames, so a retreat reads
 * the ground it is retreating FROM. A poke smaller than
 * {@link #MIN_FIGHT_UNITS} never sets it: a lone scout out on the map
 * must not pin the whole army's ladder to no-man's-land. The rung only
 * ever descends during a game, because each new fight overwrites the
 * old ground - and a fresh fight is what produced the retreat in the
 * first place.</p>
 */
public final class RetreatLadder {

    /**
     * Depth answer while no squad fight has been recorded.
     */
    public static final double NO_FIGHT_RECORDED = -1;

    /**
     * The squad bar: below this headcount the ground is a poke, not a
     * fight of ours (owner's own "4+ combat units" bar from the
     * Protoss standoff rule).
     */
    public static final int MIN_FIGHT_UNITS = 4;
    /**
     * Depth from the main of the ground the army last fought on.
     */
    private static double lastFightDepth = NO_FIGHT_RECORDED;

    private RetreatLadder() {
    }

    /**
     * Per-game reset next to the other static resets (BattleCommander).
     */
    public static void reset() {
        lastFightDepth = NO_FIGHT_RECORDED;
    }

    /**
     * The fight funnel reports the ground it is standing on: record how
     * far out that is from the main. Null-safe and never throws - an
     * unreadable world simply keeps the previous rung.
     */
    public static void noteFightGround(HasPosition ground, int armySize) {
        if (ground == null || armySize < MIN_FIGHT_UNITS) return;

        try {
            AUnit main = Select.main();
            if (main == null || main.position() == null) return;

            double depth = ground.distTo(main.position());
            if (depth < 0) return;

            lastFightDepth = depth;
        } catch (RuntimeException ignored) {
        }
    }

    /**
     * The depth of the last recorded squad fight, or {@link #NO_FIGHT_RECORDED}.
     */
    public static double lastFightDepth() {
        return lastFightDepth;
    }

    /**
     * Pure ladder: which rung a fight at {@code foughtDepth} drops to,
     * given the two line depths (both distances to the main). The
     * middle rung only exists above the natural one - a world where the
     * "middle" choke sits inboard of the natural must never hand back a
     * rung deeper than the natural choke, and with nothing recorded the
     * answer is today's behavior: the natural choke.
     */
    public static Rung rungFor(double foughtDepth, double naturalDepth, double middleDepth) {
        if (middleDepth > naturalDepth && foughtDepth > middleDepth) return Rung.MIDDLE;

        return Rung.NATURAL;
    }

    /**
     * Where the retreat may stop, from the forward side down.
     */
    public enum Rung {
        MIDDLE,
        NATURAL
    }
}
