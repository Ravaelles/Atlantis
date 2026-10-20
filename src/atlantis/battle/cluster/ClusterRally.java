package atlantis.battle.cluster;

import atlantis.battle.formation.FormationSlots;
import atlantis.battle.kite.CombatTuning;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.move.MoveAway;
import atlantis.units.select.Count;

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
     */
    public static HasPosition formationSlotFor(AUnit unit, AUnit leader, HasPosition anchor) {
        if (Alpha.get().size() < FormationSlots.MIN_CLUSTER_SIZE) return null;
        if (leader == null || !leader.isAlive()) return null;
        AUnit pivot = atlantis.battle.targeting.ASquadTargeting
            .nearestEngageableEnemy(leader, 30);
        if (pivot == null) return null;
        return FormationSlots.slotFor(unit, Alpha.get().selection(), pivot);
    }
}
