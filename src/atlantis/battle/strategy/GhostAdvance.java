package atlantis.battle.strategy;

import atlantis.battle.cluster.ClusterTactic;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.HashMap;
import java.util.Map;

/**
 * No visible enemy, but the squad remembers where the fight was: creep
 * toward the ghost instead of standing in formation.
 *
 * <p>Measured shape (owner, Wine duel): one Dragoon stepped 2-3 tiles with
 * {@code ClusterHunt}, then the formation fallback bunched it back on the
 * leader - forward, back, forward, back, never farther than 2-3 tiles from
 * its slot. The hunt owns the far case (baseless, contact beyond 9 tiles);
 * inside it the gather rule owned the unit and marched it home. This owns
 * the near-ghost case on every map: while the evaluator says FIGHT and a
 * remembered point sits 2-12 tiles ahead, the line creeps after it in
 * small steps instead of standing.</p>
 *
 * <p>Two details make the scout real instead of nominal. First, equal
 * speed with per-frame re-issues never separates anyone, so the line
 * holds one cadence while the scout walks - the lowest-id unit (the
 * same pick as the choke bait, so the whole line agrees) steps out
 * immediately, the rest follow after {@code HEAD_START_FRAMES}. Second,
 * reaching the ghost holds instead of falling through: without the
 * hold the formation fallback would bunch the scout back the moment it
 * arrives, which is exactly the loop this replaces.</p>
 *
 * <p>Priority: the choke answers still win (collapse, bait, hold are
 * consulted first by the caller) - this never pushes a held funnel. FLEE
 * never advances, and the UMS opening stand-ground keeps its 15 seconds:
 * scouting out of the opening line is feeding, not recon. A wall-pushing
 * creep gives up after {@code STUCK_FRAMES} with no progress instead of
 * grinding forever.</p>
 */
public final class GhostAdvance {

    /** Closer than this the ghost is reached: hold, never jitter on it. */
    public static final double REACHED_TILES = 2.0;

    /**
     * Farther than this the contact is stale: the army-level march (anchor,
     * unexplored starts) owns the unit, not the creep. 12 is the fight's
     * 9-tile reach plus one scout step - warm contact just outside the
     * envelope, never a cross-map expedition.
     */
    public static final double LOST_TILES = 12.0;

    /** The scout walks ahead; the line creeps behind it. */
    public static final double SCOUT_STEP_TILES = 3.0;
    public static final double LINE_STEP_TILES = 1.5;

    /** The line holds this long to let the scout go first, then follows. */
    public static final int HEAD_START_FRAMES = 24;

    /** No position change this long while creeping: give up, rally elsewhere. */
    public static final int STUCK_FRAMES = 100;

    private static final Map<Integer, Integer> firstGhostFrame = new HashMap<>();
    private static final Map<Integer, APosition> lastCreepPos = new HashMap<>();
    private static final Map<Integer, Integer> lastCreepFrame = new HashMap<>();

    private GhostAdvance() {
    }

    public static void clearForTests() {
        firstGhostFrame.clear();
        lastCreepPos.clear();
        lastCreepFrame.clear();
    }

    /**
     * Creep, hold, or give up. True when this unit is owned here for the
     * frame (an order issued, or deliberately riding/holding with no new
     * order) - the caller must not fall through to the formation then.
     * False when someone else owns it (no ghost, wrong tactic, stale
     * contact, wall grind): the formation fallback takes over.
     */
    public static boolean move(AUnit unit, FightOrFlight.Tactic tactic) {
        if (unit == null || !unit.isAlive()) return false;
        if (tactic != FightOrFlight.Tactic.FIGHT || ClusterTactic.earlyUmsStandGround()) {
            forget(unit);
            return false;
        }

        // A visible target belongs to the fight branch above the caller -
        // never shadow it with a creep order.
        if (atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 9) != null) {
            forget(unit);
            return false;
        }

        APosition ghost = ghostPointFor(unit);
        if (ghost == null) {
            forget(unit);
            return false;
        }
        double dist;
        try {
            dist = unit.distTo(ghost);
        } catch (Exception ignored) {
            forget(unit);
            return false;
        }
        if (dist < REACHED_TILES) return true;
        // UMS far march (NEXT #177): with no visible enemy in reach, a
        // ghost beyond the contact envelope is still the only address
        // the army has - closing beats standing, so the LOST give-up
        // does not apply (melee keeps it: cross-map marches there are
        // reinforcement calls, not scouting).
        if (dist > LOST_TILES && !atlantis.game.A.isUms()) {
            forget(unit);
            return false;
        }

        Integer first = firstGhostFrame.get(unit.id());
        if (first == null) {
            first = atlantis.game.A.now();
            firstGhostFrame.put(unit.id(), first);
        }
        if (!isVanguard(unit) && atlantis.game.A.now() - first < HEAD_START_FRAMES) return true;

        if (grindingAWall(unit)) {
            forget(unit);
            return false;
        }

        double step = isVanguard(unit) ? SCOUT_STEP_TILES : LINE_STEP_TILES;
        APosition dest;
        try {
            dest = unit.position().translateTilesTowards(ghost, Math.min(step, dist));
        } catch (Exception ignored) {
            return false;
        }
        if (dest != null && unit.move(dest, Actions.MOVE_ENGAGE, advanceTagFor(unit))) {
            noteProgress(unit);
            atlantis.battle.FightTarget.markRepositioned(unit);
            return true;
        }
        return false;
    }

    /** A creep that makes no progress is a wall grind: hand the unit back. */
    private static boolean grindingAWall(AUnit unit) {
        int now = atlantis.game.A.now();
        Integer lastFrame = lastCreepFrame.get(unit.id());
        APosition lastPos = lastCreepPos.get(unit.id());
        if (lastFrame != null && lastPos != null
            && now - lastFrame >= STUCK_FRAMES && unit.distTo(lastPos) < 0.5) {
            return true;
        }
        return false;
    }

    private static void noteProgress(AUnit unit) {
        lastCreepFrame.put(unit.id(), atlantis.game.A.now());
        lastCreepPos.put(unit.id(), unit.position());
    }

    private static void forget(AUnit unit) {
        firstGhostFrame.remove(unit.id());
        lastCreepPos.remove(unit.id());
        lastCreepFrame.remove(unit.id());
    }

    /**
     * The creep destination for this unit, or null when someone else owns
     * it (fighting, fleeing, opening stand-ground, choke point, no ghost).
     * The moving entry point is {@link #move}; this stays as the pure
     * seam the unit tests pin without running frames.
     */
    public static APosition advancePointFor(AUnit unit, FightOrFlight.Tactic tactic) {
        if (unit == null || !unit.isAlive()) return null;
        if (tactic != FightOrFlight.Tactic.FIGHT) return null;
        if (ClusterTactic.earlyUmsStandGround()) return null;

        // A visible target belongs to the fight branch above the caller -
        // never shadow it with a creep order.
        if (atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 9) != null) return null;

        APosition ghost = ghostPointFor(unit);
        if (ghost == null) return null;

        double dist;
        try {
            dist = unit.distTo(ghost);
        } catch (Exception ignored) {
            return null;
        }
        if (dist < REACHED_TILES
            || (dist > LOST_TILES && !atlantis.game.A.isUms())) return null;

        double step = isVanguard(unit) ? SCOUT_STEP_TILES : LINE_STEP_TILES;
        try {
            return unit.position().translateTilesTowards(ghost, Math.min(step, dist));
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Move tag for the creep: the scout walks out, the line follows. */
    public static String advanceTagFor(AUnit unit) {
        return isVanguard(unit) ? "ClusterScout" : "ClusterGhost";
    }

    /** Exactly one unit walks ahead: the lowest-id combat unit, like the choke bait. */
    public static boolean isVanguard(AUnit unit) {
        if (unit == null) return false;
        try {
            Selection ours = Select.ourCombatUnits();
            if (ours == null || ours.count() == 0) return true;
            AUnit scout = SquadScout.pickBait(ours);
            return scout != null && scout.id() == unit.id();
        } catch (Exception ignored) {
            return true;
        }
    }

    /**
     * Where the enemy was, newest memory first: this unit's own last
     * contact (snapshotted while visible), else the nearest remembered
     * enemy with a position that is not visible now. Null when nobody
     * remembers anything - then the formation fallback owns the unit.
     */
    public static APosition ghostPointFor(AUnit unit) {
        APosition last = atlantis.battle.FightTarget.lastContact(unit);
        if (last != null) return last;
        try {
            Selection seen = EnemyUnits.discovered();
            if (seen == null) return null;
            Selection foes = seen.combatUnits();
            if (foes == null) return null;
            AUnit nearest = null;
            double best = Double.MAX_VALUE;
            for (AUnit foe : foes.list()) {
                if (foe == null || foe.isVisibleUnitOnMap()) continue;
                if (!foe.hasPosition() || foe.position() == null) continue;
                double dist = unit.distTo(foe);
                if (dist < best) {
                    best = dist;
                    nearest = foe;
                }
            }
            return nearest == null ? null : nearest.position();
        } catch (Exception ignored) {
            return null;
        }
    }
}
