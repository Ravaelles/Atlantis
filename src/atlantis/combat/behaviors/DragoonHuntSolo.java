package atlantis.combat.behaviors;

import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

/**
 * Dragoon solo hunt in the early game (owner 2026-11-09): a shielded
 * Dragoon whose gun is about ready does not hold formation against
 * Zerglings - it meets the nearest one instead. Melee-only boards
 * only (any known enemy ranged unit keeps the hold), and only while
 * the shot is more than 2 seconds old with cooldown nearly gone - a
 * mid-swing Dragoon finishes its duel instead of picking a new one.
 *
 * <p>Consulted from {@code FormationHold.decide}, below the harass
 * and the kite gates: a threatened unit still kites first, a healthy
 * duel still harasses with its twin - this owns the rest. The fight
 * loop kites as needed from there.</p>
 */
public final class DragoonHuntSolo {

    /**
     * Shields at or above this mean the Dragoon may hunt.
     */
    public static final int HUNT_MIN_SHIELDS = 15;

    /**
     * The last shot must be older than this (seconds).
     */
    public static final double HUNT_MIN_SECONDS_SINCE_SHOT = 2;

    /**
     * The gun must be at least this ready (cooldown frames left).
     */
    public static final int HUNT_MAX_COOLDOWN = 8;

    private DragoonHuntSolo() {
    }

    public static boolean applies(AUnit unit, AUnit target) {
        try {
            if (unit == null || !unit.isAlive() || !unit.isDragoon()) return false;
            if (target == null || !target.isAlive() || !target.isZergling()) return false;
            if (!GamePhase.isEarlyGame()) return false;
            if (EnemyUnits.ranged() > 0) return false;
            if (Count.dragoons() < 1) return false;
            if (unit.shields() < HUNT_MIN_SHIELDS) return false;
            if (unit.shotSecondsAgo() <= HUNT_MIN_SECONDS_SINCE_SHOT) return false;
            if (unit.cooldown() > HUNT_MAX_COOLDOWN) return false;

            return true;
        } catch (Exception | Error ignored) {
            return false;
        }
    }
}
