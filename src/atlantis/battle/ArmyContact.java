package atlantis.battle;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

/**
 * Where the army last fought (NEXT #181): when one part of the cluster
 * is being shredded while another part holds formation slots with no
 * target, the idle part must join the fight instead of waiting. Every
 * engaged unit reports its target here through the fight funnel, so an
 * idle unit can march at the freshest contact instead of standing.
 *
 * <p>Bounded by design: contacts older than {@link #FRESH_FRAMES} are
 * forgotten (a finished skirmish must not drag the army back), and a
 * unit already at the fight is not re-tasked.
 */
public final class ArmyContact {

    /** A contact older than this is history, not a destination (10 game seconds). */
    public static final int FRESH_FRAMES = 240;

    /** Already fighting next to it: join nothing. */
    public static final double ARRIVED_TILES = 5.0;

    private static APosition lastFightAt = null;
    private static int lastFightFrame = -1000000;

    private ArmyContact() {
    }

    public static void clearForTests() {
        lastFightAt = null;
        lastFightFrame = -1000000;
    }

    /** Every engaged unit reports through the fight funnel. */
    public static void noteFight(AUnit unit, AUnit target) {
        if (unit == null || target == null || target.position() == null) return;
        lastFightAt = target.position();
        lastFightFrame = A.now();
    }

    /**
     * Where an idle unit should march: the freshest fight, or null when
     * there is none, it went stale, or the unit is already there. The
     * caller owns the tactic gate - this only answers the address.
     */
    public static APosition reinforcePointFor(AUnit unit) {
        if (unit == null || !unit.isAlive() || lastFightAt == null) return null;
        if (A.now() - lastFightFrame > FRESH_FRAMES) return null;
        try {
            if (unit.distTo(lastFightAt) <= ARRIVED_TILES) return null;
        } catch (Exception ignored) {
            return null;
        }
        return lastFightAt;
    }
}
