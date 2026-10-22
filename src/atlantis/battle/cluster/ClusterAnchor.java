package atlantis.battle.cluster;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Where the one cluster belongs. Pure location answers, no orders, no
 * state - the commander decides posture and tactic, this only names the
 * point on the map. Separated so anchor logic changes without touching
 * engagement logic (SRP: locate vs fight).
 */
public class ClusterAnchor {

    /** The anchor for this posture: the enemy base to attack, home to defend. */
    public static HasPosition anchorFor(ClusterMission.Posture posture) {
        return posture == ClusterMission.Posture.ATTACK ? attackAnchor() : defendAnchor();
    }

    public static HasPosition defendAnchor() {
        return Select.mainOrAnyBuilding();
    }

    public static HasPosition attackAnchor() {
        AUnit base = EnemyUnits.enemyBase();
        if (base != null) return base.position();

        HasPosition main = EnemyUnits.enemyMainBase();
        if (main != null) return main;

        // Unknown enemy: march on the nearest unexplored start location
        // (UAlbertaBot's "explore until we find a target"). Sitting home
        // on ATTACK played 18 minutes to 0/0 - measured 2026-10-17.
        // Baseless (lost base, UMS duel maps): there is no main to march
        // from, so reference the army itself - otherwise the anchor is
        // null and the cluster stands still where it stands (measured
        // Wine 1Drag_v_1Zeal: one dragoon idle a whole game).
        HasPosition from = Select.mainOrAnyBuilding();
        if (from == null) from = Alpha.get().center();

        HasPosition unexplored = from == null
            ? null
            : BaseLocations.nearestUnexploredStartingLocation(from);
        if (unexplored != null) return unexplored;

        return Select.mainOrAnyBuilding();
    }
}
