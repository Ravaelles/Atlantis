package atlantis.combat.behaviors;

import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

/**
 * Corsairs hunt instead of idling (NEXT #198, port of the old
 * CorsairHuntOverlords gate): against Zerg, an unmolested Corsair with
 * an Overlord on the board closes to firing distance and kills it
 * instead of waiting for targets to wander in range. Dark Archon food
 * stays home: a Corsair under attack keeps flying, it does not chase.
 */
public final class CorsairHunt {

    private CorsairHunt() {
    }

    /**
     * Only unmolested Corsairs hunt, and only when Zerg flies Overlords.
     */
    public static boolean applies(AUnit unit) {
        if (unit == null || !unit.isAlive()) return false;
        if (!unit.isCorsair()) return false;
        if (!Enemy.zerg()) return false;
        if (unit.lastUnderAttackLessThanAgo(30 * 3)) return false;

        return huntTarget(unit) != null;
    }

    /**
     * Close to firing distance while cooling, shoot on ready. True when
     * an order went out.
     */
    public static boolean act(AUnit unit) {
        AUnit overlord = applies(unit) ? huntTarget(unit) : null;
        if (overlord == null) return false;

        if (unit.cooldown() >= 10 && unit.distTo(overlord) >= 5) {
            return unit.move(overlord, Actions.MOVE_DANCE_TO);
        }

        return unit.attackUnit(overlord);
    }

    /**
     * The closest wounded-or-near Overlord, else the nearest known one -
     * the old gate's shape (close kills first, the board is searched
     * only when nothing is close).
     */
    static AUnit huntTarget(AUnit unit) {
        Selection overlords = unit.enemiesNear().overlords();
        if (overlords == null || overlords.isEmpty()) return null;

        Selection close = overlords.inRadius(6, unit);
        if (close.notEmpty()) {
            return close.mostWoundedOrNearest(unit);
        }

        Selection known = EnemyUnits.discovered().overlords();
        if (known == null || known.isEmpty()) return null;

        return known.nearestTo(unit);
    }
}
