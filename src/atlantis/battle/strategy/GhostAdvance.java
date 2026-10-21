package atlantis.battle.strategy;

import atlantis.battle.cluster.ClusterTactic;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

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
 * small steps instead of standing, and exactly one unit (the same
 * lowest-id pick as the choke bait, so the whole line agrees) walks
 * ahead as the scout the owner asked for.</p>
 *
 * <p>Priority: the choke answers still win (collapse, bait, hold are
 * consulted first by the caller) - this never pushes a held funnel. FLEE
 * never advances, and the UMS opening stand-ground keeps its 15 seconds:
 * scouting out of the opening line is feeding, not recon.</p>
 */
public final class GhostAdvance {

    /** Closer than this the ghost is reached: hold, never jitter on it. */
    public static final double REACHED_TILES = 2.0;

    /**
     * Farther than this the contact is stale: the army-level march (anchor,
     * unexplored starts) owns the unit, not the creep. 12 is the fight's
     * 9-tile reach plus one scout step - warm contact just outside the
     * envelope, never a cross-map expedition (the sim
     * {@code DragoonsVsZerglingsTest.noHuntOnBasedMaps} pins the 17-tile
     * shape stays put).
     */
    public static final double LOST_TILES = 12.0;

    /** The scout walks ahead; the line creeps behind it. */
    public static final double SCOUT_STEP_TILES = 3.0;
    public static final double LINE_STEP_TILES = 1.5;

    private GhostAdvance() {
    }

    /**
     * The creep destination for this unit, or null when someone else owns
     * it (fighting, fleeing, opening stand-ground, choke point, no ghost).
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
        if (dist < REACHED_TILES || dist > LOST_TILES) return null;

        double step = isVanguard(unit) ? SCOUT_STEP_TILES : LINE_STEP_TILES;
        try {
            return unit.position().translateTilesTowards(ghost, Math.min(step, dist));
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Move tag for the creep above: the scout walks out, the line follows. */
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
