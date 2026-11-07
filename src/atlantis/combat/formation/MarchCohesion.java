package atlantis.combat.formation;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The march travels abreast, never in single file (owner order
 * 2026-11-04): a strung-out line is the worst shape there is. Spread is
 * the stddev of the cluster's distances to the next choke on the way -
 * near zero for a line abreast, big for a file along the march axis -
 * and the front holds while the rear dresses. The rear never steps
 * back, so the spread only shrinks: no yo-yo. Hysteresis (hold at 4+,
 * release below 2.5) keeps the boundary from dithering, the same
 * reason the collapse latch exists.
 *
 * <p>Pure over numbers, so the contract pins in milliseconds. Live
 * wiring (mate distances, choke resolution, per-unit hold) stays
 * thin and delegates here.</p>
 */
public final class MarchCohesion {

    /**
     * Stddev at or above this: the line is scattered, the front holds.
     */
    public static final double HOLD_SPREAD_TILES = 4.0;

    /**
     * Stddev below this: the line is dressed, the hold releases. Below
     * HOLD, so a boundary hover never flips the verdict every frame.
     */
    public static final double RELEASE_SPREAD_TILES = 2.5;

    /**
     * Owner: the hold steps back this far from the choke (nextChoke
     * moveAway), never marches home.
     */
    public static final double FALLBACK_TILES = 2.0;

    /**
     * Fewer units than this dress by bunching, not by the gate.
     */
    public static final int MIN_UNITS = 3;

    public enum Verdict {
        ADVANCE,
        REGROUP
    }

    private MarchCohesion() {
    }

    /**
     * Population stddev of distances to the choke. Fewer than two units
     * read zero: nothing to dress yet.
     */
    public static double spreadOf(List<Double> distances) {
        if (distances == null || distances.size() < 2) return 0.0;

        double sum = 0.0;
        for (double dist : distances) sum += dist;
        double mean = sum / distances.size();

        double variance = 0.0;
        for (double dist : distances) variance += (dist - mean) * (dist - mean);

        return Math.sqrt(variance / distances.size());
    }

    public static double meanOf(List<Double> distances) {
        if (distances == null || distances.isEmpty()) return 0.0;

        double sum = 0.0;
        for (double dist : distances) sum += dist;

        return sum / distances.size();
    }

    /**
     * Hold the front when scattered, release when dressed - with
     * hysteresis, so a boundary hover keeps its current answer.
     */
    public static Verdict decide(double stddev, boolean holding) {
        if (holding) return stddev < RELEASE_SPREAD_TILES ? Verdict.ADVANCE : Verdict.REGROUP;

        return stddev >= HOLD_SPREAD_TILES ? Verdict.REGROUP : Verdict.ADVANCE;
    }

    /**
     * This unit holds when the line is scattered AND it is ahead of the
     * pack (closer to the choke than the mean). The rear always advances
     * into its slots, so a hold never parks the whole army - it lets the
     * tail catch up.
     */
    public static boolean shouldHold(double myDist, double mean, double stddev, boolean holding) {
        if (decide(stddev, holding) == Verdict.ADVANCE) return false;

        return myDist < mean;
    }

    /**
     * A step back from the choke, away from the enemy: two tiles (owner:
     * nextChoke.moveAway(2)), never a march home. The tile must be
     * walkable - an unwalkable hold is a refused order plus a stack
     * trace every time (measured live: Dragoon ordered into a wall off
     * a choke fallback). Spirals like the collapse staging point; null
     * when the whole pocket is unwalkable, and the caller falls through
     * to the normal legs instead of issuing a cursed order.
     */
    public static APosition fallbackPoint(HasPosition choke, HasPosition awayFrom) {
        if (choke == null) return null;

        try {
            APosition center = APosition.create(choke);
            if (awayFrom != null) {
                APosition foe = APosition.create(awayFrom);
                double dx = center.tx() - foe.tx();
                double dy = center.ty() - foe.ty();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist >= 0.01) {
                    center = APosition.create(
                        (int) Math.round(center.tx() + dx / dist * FALLBACK_TILES),
                        (int) Math.round(center.ty() + dy / dist * FALLBACK_TILES));
                }
            }

            return firstWalkableNear(center, APosition::isWalkable);
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    /**
     * Nearest walkable tile to the center, the center first, then rings
     * 1-3 out (same search as the collapse staging). The walkability
     * check rides a predicate so the spiral pins without a game.
     * Public as a seam: any ordered move can validate its destination
     * through the same search instead of growing its own.
     */
    public static APosition firstWalkableNear(
        APosition center, java.util.function.Predicate<APosition> walkable
    ) {
        if (center == null || walkable == null) return null;

        try {
            if (walkable.test(center)) return center;

            for (int r = 1; r <= 3; r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dy = -r; dy <= r; dy++) {
                        if (Math.max(Math.abs(dx), Math.abs(dy)) != r) continue;

                        APosition candidate = APosition.create(center.tx() + dx, center.ty() + dy);
                        if (walkable.test(candidate)) return candidate;
                    }
                }
            }

            return null;
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    /**
     * Units currently held for cohesion (ids). Cleared per game/test
     * with the other battle resets - a stale hold would park a unit
     * forever, the same class of leak the collapse latch guards.
     */
    private static final Set<Integer> HOLDING = new HashSet<>();

    public static boolean holding(int unitId) {
        return HOLDING.contains(unitId);
    }

    public static void setHolding(int unitId, boolean hold) {
        if (hold) HOLDING.add(unitId);
        else HOLDING.remove(unitId);
    }

    public static void clearForTests() {
        HOLDING.clear();
    }

    /**
     * The choke the spread is measured against: the next choke on the
     * path toward the enemy (owner: distance to the next choke relative
     * to the enemy), else the nearest choke to the march anchor, else
     * the anchor itself (spread to a single forward point still reads a
     * snake). Null-safe: never null when the anchor isn't.
     */
    static HasPosition resolveChoke(HasPosition anchor) {
        if (anchor == null) return null;

        try {
            java.util.ArrayList<atlantis.map.choke.AChoke> path =
                atlantis.map.path.PathToEnemyBase.chokesLeadingToEnemyBase();
            if (path != null && !path.isEmpty() && path.get(0) != null) return path.get(0);
        } catch (Exception | Error ignored) {
        }
        try {
            atlantis.map.choke.AChoke near = atlantis.map.choke.Chokes.nearestChoke(anchor);
            if (near != null) return near;
        } catch (Exception | Error ignored) {
        }

        return anchor;
    }

    /**
     * Live entry: hold point for this unit, or null when it advances
     * normally. Collects the cluster's distances to the resolved choke,
     * applies the hysteresis verdict, and latches the outcome - a
     * boundary hover keeps its answer instead of flipping orders every
     * frame (the same dither the collapse latch was built for). Twins
     * transitions only (engage/release with the spread numbers).
     */
    public static APosition holdPointFor(AUnit unit, HasPosition anchor) {
        if (unit == null || anchor == null || !unit.isAlive()) return null;

        try {
            HasPosition choke = resolveChoke(anchor);
            if (choke == null) return null;

            // Absolute main-choke ban (owner order 2026-11-07): the dress
            // holds the line at the choke fallback, which must not be
            // the banned main choke - the front marches on instead of
            // dressing where the focus refuses to stage.
            if (choke instanceof atlantis.map.choke.AChoke
                && ((atlantis.map.choke.AChoke) choke).isMainChoke()
                && atlantis.combat.focus.ProtossDefendFocusPoint.mainChokeBanned()) return null;

            List<AUnit> mates = Select.ourCombatUnits().list();
            List<Double> dists = new ArrayList<>();
            double myDist = Double.MAX_VALUE;
            int counted = 0;
            for (AUnit mate : mates) {
                if (mate == null || !mate.isAlive()) continue;
                // The vanguard scout runs ahead by design (it owns the
                // contact); pacing the body against the scout would pin
                // everyone behind one runner forever. The asker always
                // counts itself.
                boolean asker = mate.id() == unit.id();
                if (!asker && isVanguard(mate)) continue;
                double dist = mate.distTo(choke);
                dists.add(dist);
                counted++;
                if (asker) myDist = dist;
            }
            if (counted < MIN_UNITS || myDist == Double.MAX_VALUE) return null;

            double mean = meanOf(dists);
            double spread = spreadOf(dists);
            boolean holding = HOLDING.contains(unit.id());
            if (!shouldHold(myDist, mean, spread, holding)) {
                if (holding) {
                    HOLDING.remove(unit.id());
                    twin(unit, spread, mean, "released");
                }

                return null;
            }
            if (!holding) {
                HOLDING.add(unit.id());
                twin(unit, spread, mean, "engaged");
            }

            return fallbackPoint(choke, anchor);
        } catch (Exception | Error ignored) {
            return null;
        }
    }

    private static boolean isVanguard(AUnit unit) {
        try {
            return atlantis.combat.strategy.GhostAdvance.isVanguard(unit);
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    private static void twin(AUnit unit, double spread, double mean, String transition) {
        try {
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.MARCH_COHESION)
                .param("unit", unit.id())
                .param("spread", String.format("%.1f", spread))
                .param("mean", String.format("%.1f", mean))
                .param("transition", transition)
                .log(atlantis.game.A.now(), atlantis.game.A.minSec());
        } catch (Exception | Error ignored) {
        }
    }
}
