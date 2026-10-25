package atlantis.combat.twins;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Per-unit fight trace (owner 2026-10-20): who stood where, at what
 * health and how far from the nearest enemy, and when each unit died.
 * Formation fights are won by shape and cooperation, not by single-unit
 * heroics - so the file carries per-unit distance/health/death-frame
 * rows a digest script (`scripts/fight-units.py`) turns into a table:
 * who died when, at what health, how far from the enemy. Sampled, not
 * pushed: combat units report every TRACE_EVERY_FRAMES while acting;
 * deaths always land, unthrottled.
 */
public final class FightTrace {

    /** One sample per cooldown cycle keeps 8v8 games near ~500 rows. */
    public static final int TRACE_EVERY_FRAMES = 30;

    private static final java.util.Map<Integer, Integer> lastSampleFrame = new java.util.HashMap<>();

    private FightTrace() {
    }

    public static void sample(AUnit unit, AUnit threat) {
        if (unit == null || !unit.isAlive() || !unit.isCombatUnit()) return;

        int now = A.now();
        Integer last = lastSampleFrame.get(unit.id());
        if (last != null && now - last < TRACE_EVERY_FRAMES) return;

        lastSampleFrame.put(unit.id(), now);
        EventLog.key(LogKey.UNIT_TRACE)
            .param("unit", unit.id())
            .param("type", unit.type().name())
            .param("hp", unit.hp())
            .param("shields", unit.shields())
            .param("dist", threat == null ? -1 : (int) (unit.distTo(threat) * 10))
            .param("ux", unit.x() / 32)
            .param("uy", unit.y() / 32)
            .log(now, A.minSec());
    }

    private static final java.util.Set<Integer> reportedDeaths = new java.util.HashSet<>();

    public static void died(AUnit unit) {
        if (unit == null) return;
        // One row per unit per game: the destroy listener can fire repeatedly
        // for the same corpse (measured live: 2 marines produced 6841 rows),
        // and the digest script joins on the unit id - duplicates read as
        // thousands of deaths. Same id set as the per-cycle sampler uses.
        if (!reportedDeaths.add(unit.id())) return;

        AUnit nearest = nearestEnemy(unit);
        double dist = nearest == null ? -1 : unit.distTo(nearest) * 10;
        if (Double.isNaN(dist) || Double.isInfinite(dist) || dist < 0 || dist > 100000) {
            // Dead-unit positions can be garbage (measured 671088639 on M&M);
            // unknown beats a number no map could produce. Matches the
            // no-enemy branch above instead of inventing a new sentinel.
            dist = -1;
        }
        EventLog.key(LogKey.UNIT_DIED)
            .param("unit", unit.id())
            .param("type", unit.type().name())
            .param("killer", nearest == null ? -1 : nearest.id())
            .param("killerType", nearest == null ? "-" : nearest.type().name())
            .param("dist", (int) dist)
            .param("ux", unit.x() / 32)
            .param("uy", unit.y() / 32)
            .log(A.now(), A.minSec());
    }

    public static void clearForTests() {
        lastSampleFrame.clear();
        reportedDeaths.clear();
    }

    // =========================================================

    private static AUnit nearestEnemy(AUnit unit) {
        Selection enemies;
        try {
            enemies = Select.enemy();
        }
        catch (RuntimeException ignored) {
            return null;
        }
        if (enemies == null) return null;

        AUnit best = null;
        double bestDist = Double.MAX_VALUE;
        for (AUnit enemy : enemies.list()) {
            if (enemy == null || !enemy.isAlive()) continue;
            double dist = unit.distTo(enemy);
            if (dist < bestDist) {
                bestDist = dist;
                best = enemy;
            }
        }
        return best;
    }
}
