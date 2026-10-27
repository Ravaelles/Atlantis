package atlantis.combat.retreating;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Where a fleeing unit rallies when the road home runs through the enemy
 * (owner 2026-10-28, tested loss vs Protoss: the army fled to the base
 * straight through a dozen zealots and died on the march - the rally was
 * always the base, never the ground the escape could actually use).
 *
 * <p>The road home is a segment, not a wish: when it passes within
 * corridor range of an enemy combat unit the rally moves to the choke
 * farthest from the enemy army and the enemy base whose own road is
 * clear. A choke sitting inside the enemy, or one reached only through
 * the enemy, is never an answer - when every road crosses, the unit
 * steps away locally ({@code EscapeScoredStep}) instead of donating a
 * march, and the old home rally is the last resort, never a null.</p>
 *
 * <p>Positions only, no orders - the caller moves. The scored overload
 * is the pinnable seam (pure geometry, no world reads); the two-arg
 * overload gathers the live world and fails open to {@code home}.</p>
 */
public final class RetreatEscapeRoute {

    /**
     * The road home is blocked when it passes this close to an enemy
     * combat unit (tiles): the march would chew through the pack instead
     * of around it. Wider than weapon range on purpose - the column has
     * width, and the tail must clear too.
     */
    public static final double BLOCKED_CORRIDOR_TILES = 6;

    /**
     * A rally this close to an enemy combat unit is marching into the
     * army, not away from it - the candidate is refused outright.
     */
    public static final double INTO_ENEMY_TILES = 8;

    private RetreatEscapeRoute() {
    }

    /**
     * The rally this unit marches to: {@code home}, unless the road
     * there runs through the enemy - then the farthest clear choke.
     * Never null when {@code home} is non-null (fail-open: a bad rally
     * still beats standing in the duel). Null-safe by construction.
     */
    public static HasPosition safeRally(AUnit unit, HasPosition home) {
        if (unit == null || home == null) return home;

        try {
            List<AUnit> foes = liveFoes();
            if (foes.isEmpty()) return home;
            if (!pathCrossesThreat(unit, home, foes, BLOCKED_CORRIDOR_TILES)) return home;

            List<HasPosition> chokes = liveChokes();
            if (chokes.isEmpty()) return home;

            HasPosition enemyBase = liveEnemyBase();
            HasPosition escape = bestEscape(unit, home, chokes, foes, enemyBase);
            if (escape != null) return escape;

            HasPosition step = localStep(unit, nearestFoe(unit, foes));

            return step != null ? step : home;
        }
        catch (RuntimeException ignored) {
            return home;
        }
    }

    /**
     * Pure-geometry seam: the farthest clear choke, or null when every
     * candidate sits inside the enemy or is reached only through it.
     * {@code home} is never returned here - the caller owns the
     * fallback chain; null means "no escape ground, step locally".
     */
    public static HasPosition bestEscape(
        AUnit unit, HasPosition home, List<? extends HasPosition> candidates,
        List<AUnit> foes, HasPosition enemyBase
    ) {
        if (unit == null || home == null || candidates == null || foes == null) return null;

        HasPosition best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (HasPosition candidate : candidates) {
            HasPosition ground = groundOf(candidate);
            if (ground == null) continue;

            double nearestFoe = nearestDist(ground, foes);
            if (nearestFoe < INTO_ENEMY_TILES) continue;
            if (pathCrossesThreat(unit, ground, foes, BLOCKED_CORRIDOR_TILES)) continue;

            double score = nearestFoe
                + 0.5 * (enemyBase == null ? 0 : safeDist(ground, enemyBase))
                - 0.25 * safeDist(unit, ground);
            if (score > bestScore) {
                bestScore = score;
                best = ground;
            }
        }

        return best;
    }

    /**
     * True when the segment from {@code from} to {@code to} passes
     * within {@code corridorTiles} of any foe: the march would run
     * through the pack. Point-segment distance in tile units, analytic
     * (no sampling grid, no allocation).
     */
    public static boolean pathCrossesThreat(
        HasPosition from, HasPosition to, List<AUnit> foes, double corridorTiles
    ) {
        if (from == null || to == null || foes == null || foes.isEmpty()) return false;

        try {
            for (AUnit foe : foes) {
                if (foe == null || !foe.isAlive()) continue;
                if (segDistTiles(from, to, foe) < corridorTiles) return true;
            }
            return false;
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    // =========================================================

    private static double segDistTiles(HasPosition a, HasPosition b, HasPosition p) {
        double ax = a.x(), ay = a.y();
        double bx = b.x(), by = b.y();
        double px = p.x(), py = p.y();

        double dx = bx - ax, dy = by - ay;
        double len2 = dx * dx + dy * dy;
        if (len2 < 1) return a.distTo(p);

        double t = ((px - ax) * dx + (py - ay) * dy) / len2;
        if (t < 0) t = 0;
        else if (t > 1) t = 1;

        double cx = ax + t * dx, cy = ay + t * dy;

        return Math.hypot(px - cx, py - cy) / 32.0;
    }

    private static HasPosition groundOf(HasPosition candidate) {
        if (candidate instanceof AChoke) {
            try {
                return ((AChoke) candidate).center();
            }
            catch (RuntimeException ignored) {
                return null;
            }
        }
        return candidate;
    }

    private static double nearestDist(HasPosition ground, List<AUnit> foes) {
        double best = Double.MAX_VALUE;
        for (AUnit foe : foes) {
            if (foe == null || !foe.isAlive()) continue;
            double d = safeDist(ground, foe);
            if (d < best) best = d;
        }
        return best;
    }

    private static AUnit nearestFoe(AUnit unit, List<AUnit> foes) {
        AUnit best = null;
        double bestDist = Double.MAX_VALUE;
        for (AUnit foe : foes) {
            if (foe == null || !foe.isAlive()) continue;
            double d = safeDist(unit, foe);
            if (d < bestDist) {
                bestDist = d;
                best = foe;
            }
        }
        return best;
    }

    private static double safeDist(HasPosition a, HasPosition b) {
        try {
            return a.distTo(b);
        }
        catch (RuntimeException ignored) {
            return Double.MAX_VALUE;
        }
    }

    private static HasPosition localStep(AUnit unit, AUnit threat) {
        if (unit == null || threat == null) return null;

        try {
            return atlantis.combat.escape.EscapeScoredStep.pickScored(unit, threat, 8);
        }
        catch (RuntimeException | Error ignored) {
            return null;
        }
    }

    // ---- live world (each read guarded, fail-open to home) ----

    private static List<AUnit> liveFoes() {
        List<AUnit> foes = new ArrayList<>();
        try {
            atlantis.units.select.Selection enemies = Select.enemy().combatUnits();
            if (enemies == null || enemies.empty()) return foes;

            for (AUnit foe : enemies.list()) {
                if (foe != null && foe.isAlive()) foes.add(foe);
            }
        }
        catch (RuntimeException ignored) {
        }
        return foes;
    }

    private static List<HasPosition> liveChokes() {
        List<HasPosition> grounds = new ArrayList<>();
        try {
            List<AChoke> chokes = Chokes.chokes();
            if (chokes == null) return grounds;

            for (AChoke choke : chokes) {
                HasPosition ground = groundOf(choke);
                if (ground != null) grounds.add(ground);
            }
        }
        catch (RuntimeException ignored) {
        }
        return grounds;
    }

    private static HasPosition liveEnemyBase() {
        try {
            AUnit base = EnemyUnits.enemyBase();
            if (base != null && base.isAlive()) return base;

            return EnemyUnits.enemyMainBase();
        }
        catch (RuntimeException ignored) {
            return null;
        }
    }
}
