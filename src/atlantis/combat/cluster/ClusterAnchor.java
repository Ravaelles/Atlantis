package atlantis.combat.cluster;

import atlantis.game.player.Enemy;
import atlantis.combat.focus.AttackFocusPoint;
import atlantis.combat.focus.ProtossDefendFocusPoint;
import atlantis.map.position.HasPosition;
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

    /**
     * Where the cluster belongs on defend, by enemy race (owner
     * 2026-10-26, from a tested loss vs Protoss): vs Zerg the line hugs
     * the main for worker support; vs Protoss it forms up 8 tiles from
     * the main choke so a lost fight has room to retreat instead of
     * wiping against the buildings. Terran keeps the old hug-the-main
     * stand - no tested direction there yet, deliberately unchanged.
     */
    public static HasPosition defendAnchor() {
        if (Enemy.protoss()) {
            HasPosition focus = ProtossDefendFocusPoint.focusPoint();
            if (focus != null) return focus;
        }

        return Select.mainOrAnyBuilding();
    }

    public static HasPosition attackAnchor() {
        return AttackFocusPoint.focusPoint();
    }
}
