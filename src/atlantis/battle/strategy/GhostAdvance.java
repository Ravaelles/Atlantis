package atlantis.battle.strategy;

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

    /**
     * The line advances half a tile, then stands 3 frames (owner
     * 2026-10-26): the march moves in formation instead of rushing -
     * Spartans in a line, not a parking lot that dissolves into a
     * charge. The vanguard keeps full steps; only the line is slow.
     */
    public static final double LINE_STEP_TILES = 0.5;

    /** Still frames between line steps: step, stand, step. */
    public static final int LINE_PAUSE_FRAMES = 3;

    /** The line holds this long to let the scout go first, then follows. */
    public static final int HEAD_START_FRAMES = 24;

    /**
     * UMS scout release (owner 2026-10-26): with no visible enemy the
     * vanguard walks after ~1 second, not after the 15-second tactical
     * stand-ground - that gate stays for the retreat decision
     * ({@code ClusterTactic.earlyUmsStandGround}), but holding the scout
     * with it left the group standing tens of seconds doing nothing.
     * 15x faster than the old 360-frame wait, still past the contact
     * frame so the vanguard never sprints before first sight.
     */
    public static final int UMS_SCOUT_RELEASE_FRAMES = 24;

    /** No position change this long while creeping: give up, rally elsewhere. */
    public static final int STUCK_FRAMES = 100;

    /**
     * Personal contact older than this is history, not a destination (10 game
     * seconds, same brake as the army-level contact): the unit falls through
     * to fogged memory, then to unexplored ground, instead of ping-ponging
     * one stale tile all game. HuntLastContact keeps its own policy (baseless
     * re-acquire) - this gate owns the UMS creep only.
     */
    public static final int STALE_CONTACT_FRAMES = 240;

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
        if (tactic != FightOrFlight.Tactic.FIGHT || scoutHeld()) {
            forget(unit);
            return false;
        }

        // A visible target in weapon range belongs to the fight branch
        // above the caller - never shadow it with a creep order. A
        // visible target OUT of range does not: when acquisition
        // refuses it (imminent shot, formation hold), the creep is the
        // approach march that closes the distance instead of idling.
        AUnit seen = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 9);
        if (seen != null) {
            try {
                if (unit.hasWeaponRangeToAttack(seen, 0)) {
                    forget(unit);
                    return false;
                }
            } catch (Exception ignored) {
                forget(unit);
                return false;
            }
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

        // The line steps, then stands: a step every LINE_PAUSE_FRAMES,
        // riding in between. Without the pause the line re-issues every
        // frame and rushes the ghost in a blob.
        if (!isVanguard(unit)) {
            Integer lastCreep = lastCreepFrame.get(unit.id());
            if (lastCreep != null && atlantis.game.A.now() - lastCreep < LINE_PAUSE_FRAMES) {
                return true;
            }
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
        if (scoutHeld()) return null;

        // A visible target in weapon range belongs to the fight branch
        // above the caller - never shadow it with a creep order. Out of
        // range, the creep is the approach march (same rule as move()).
        AUnit seenForPoint = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 9);
        if (seenForPoint != null) {
            try {
                if (unit.hasWeaponRangeToAttack(seenForPoint, 0)) return null;
            } catch (Exception ignored) {
                return null;
            }
        }

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

    /**
     * No contact memory and no fogged enemies, but the map is not fully
     * explored (NEXT #177): march at the nearest unexplored region
     * instead of standing - hidden enemies live where sight never was.
     * UMS only: melee keeps formation when blind. Null when everything
     * is explored (cloaked leftovers are a detector problem, not a
     * marching one) or the region library has no answer.
     */
    private static APosition unexploredDestinationFor(AUnit unit) {
        if (unit == null || !atlantis.game.A.isUms()) return null;
        try {
            atlantis.map.region.ARegion region =
                atlantis.map.region.Regions.getNearestUnexploredRegion(unit.position());
            if (region == null || region.center() == null) return null;
            return region.center();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * This unit's own contact, only while fresh: a stale tile belongs to
     * fogged memory or exploration, never to the creep. Null-safe by
     * construction (no frame recorded means no contact).
     */
    private static APosition freshContactFor(AUnit unit) {
        if (unit == null) return null;
        APosition last = atlantis.battle.FightTarget.lastContact(unit);
        if (last == null) return null;
        try {
            Integer frame = atlantis.battle.FightTarget.lastContactFrame(unit);
            if (frame == null) return null;
            if (atlantis.game.A.now() - frame > STALE_CONTACT_FRAMES) return null;
        } catch (Exception ignored) {
            return null;
        }
        return last;
    }

    /**
     * The scout owns the first step out: one frame past contact the
     * vanguard is already gone, the line never waits the tactical
     * stand-ground out. Only the UMS opening holds it, and only for
     * {@link #UMS_SCOUT_RELEASE_FRAMES} - the 15-second
     * {@code ClusterTactic.earlyUmsStandGround} still owns the retreat
     * decision above, it just no longer pins the scout with it.
     */
    private static boolean scoutHeld() {
        return atlantis.game.A.isUms() && atlantis.game.A.now() < UMS_SCOUT_RELEASE_FRAMES;
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
        APosition last = freshContactFor(unit);
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
            if (nearest != null) return nearest.position();
        } catch (Exception ignored) {
        }
        return unexploredDestinationFor(unit);
    }
}
