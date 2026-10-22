package atlantis.battle.cluster;

import atlantis.battle.formation.FormationSlots;
import atlantis.battle.kite.CombatTuning;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.move.MoveAway;

/**
 * Where the cluster and its units move when they are not fighting
 * (extracted from BattleCommander): baseless regroup points away from
 * the threat, leader bunching on the march, formation slots when the
 * cluster forms up. Positions only, no orders - the caller moves.
 */
public final class ClusterRally {

    private ClusterRally() {
    }

    /** Tiles behind the leader before a unit turns back to rejoin. */
    private static final double CLUSTER_GATHER_RADIUS_TILES = 2;

    /**
     * Where a baseless retreat rallies (owner): a point away from the
     * nearest threat to regroup at, never the enemy base the mission
     * anchor would march it to. Null when nothing presses (then the
     * caller fights in place) or when no direction steps - same
     * fall-through contract as the kite escape. Public as a seam.
     */
    public static HasPosition regroupRallyPoint(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;
        AUnit threat = atlantis.battle.targeting.ASquadTargeting
            .nearestEngageableEnemy(unit, CombatTuning.current().getRegroupThreatTiles());
        if (threat == null) return null;
        return MoveAway.bestDestination(unit, threat, CombatTuning.current().getRegroupRallyTiles());
    }

    /**
     * Where this unit moves when there is no enemy to engage: the leader
     * when straggling behind it, else the mission anchor. Public as a seam:
     * the bunching rule pins in a stub state without running the frame.
     */
    public static HasPosition gatherPointFor(AUnit unit, AUnit leader, HasPosition anchor) {
        if (leader != null && unit != leader && unit.distTo(leader) > CLUSTER_GATHER_RADIUS_TILES) {
            return leader;
        }
        return anchor;
    }

    /**
     * Formation slot for a unit with no enemy in reach (Phase A): the
     * crescent around the leader's nearest enemy. Null when the
     * formation does not apply (too few units, no pivot, pivot in
     * contact, no walkable slot) - the caller bunches on the leader.
     *
     * <p>No visible pivot does not mean no direction (owner 2026-10-26):
     * on FIGHT the blind crescent still faces where the enemy is
     * expected - the last-seen enemy center on UMS, the main choke at
     * the home base on melee, the mission anchor past that - instead
     * of collapsing into a clump facing nowhere. On FLEE only a
     * visible pivot forms up (retreats never march at memories);
     * truly blind on FIGHT still bunches, honestly standing instead
     * of guessing.</p>
     */
    public static HasPosition formationSlotFor(
        AUnit unit, AUnit leader, HasPosition anchor, FightOrFlight.Tactic tactic
    ) {
        if (Alpha.get().size() < FormationSlots.MIN_CLUSTER_SIZE) return null;
        if (leader == null || !leader.isAlive()) return null;
        HasPosition pivot = atlantis.battle.targeting.ASquadTargeting
            .nearestEngageableEnemy(leader, 30);
        if (pivot == null && tactic == FightOrFlight.Tactic.FIGHT) {
            pivot = expectedDirectionPoint(leader, anchor);
        }
        if (pivot == null) return null;
        return FormationSlots.slotFor(unit, Alpha.get().selection(), pivot);
    }

    /**
     * Where the enemy is expected when nobody sees it: the whole ghost
     * chain (fresh contact, then fogged memory, then unexplored ground)
     * on UMS; the main choke when the cluster holds the home base on
     * melee; the mission anchor past that (the march direction itself).
     * Null-safe by construction - the choke library and the ghost chain
     * both answer null outside their world.
     */
    private static HasPosition expectedDirectionPoint(AUnit leader, HasPosition anchor) {
        try {
            if (atlantis.game.A.isUms()) {
                APosition ghost = atlantis.battle.strategy.GhostAdvance.ghostPointFor(leader);
                if (ghost != null) return ghost;
            }
            else if (atlantis.battle.cluster.ClusterTactic.holdsHomeGround()) {
                atlantis.map.choke.AChoke choke = atlantis.map.choke.Chokes.mainChoke();
                if (choke != null && choke.center() != null) return choke.center();
            }
        } catch (Exception ignored) {
        }
        return anchor;
    }
}
