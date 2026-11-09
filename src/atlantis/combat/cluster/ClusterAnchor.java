package atlantis.combat.cluster;

import atlantis.combat.focus.AttackFocusPoint;
import atlantis.combat.focus.ProtossDefendFocusPoint;
import atlantis.game.player.Enemy;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Select;

/**
 * Where the one cluster belongs. Pure location answers, no orders, no
 * state - the commander decides posture and tactic, this only names the
 * point on the map. Separated so anchor logic changes without touching
 * engagement logic (SRP: locate vs fight).
 */
public class ClusterAnchor {

    /**
     * The anchor for this posture: the enemy base to attack, home to defend.
     */
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
     * Absolute ban (owner order): a Protoss dragoon mass over 10 stages
     * at the middle on every matchup - the small-army home doctrines
     * above never hold a finished army back.
     *
     * <p>Owner 2026-11-09 (tech turtle): with a second base standing the
     * line never falls back to the main choke - only the natural choke
     * (or a base) answers, on every matchup. The focus already withdraws
     * the main input then; the anchor must consult it there too, not
     * just vs Protoss. The MainChoke answer stays on the old matchup
     * doctrine (the unknown-natural shape is unobserved).</p>
     */
    public static HasPosition defendAnchor() {
        if (Enemy.protoss() || ProtossDefendFocusPoint.mainChokeBannedByDragoons()) {
            HasPosition focus = ProtossDefendFocusPoint.focusPoint();
            if (focus != null) return focus;
        }

        if (isMultiBase()) {
            HasPosition focus = ProtossDefendFocusPoint.focusPoint();
            if (focus != null && !isMainChokeAnswer(focus)) return focus;
        }

        return Select.mainOrAnyBuilding();
    }

    /**
     * Two or more bases standing (same count the focus reads): the shape
     * where the main choke is withdrawn and only the natural answers.
     */
    private static boolean isMultiBase() {
        try {
            return Select.ourWithUnfinished().bases().count() >= 2;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * The one focus answer the multibase branch does not take: the old
     * main-choke standoff still belongs to the matchup doctrine above
     * (vs Zerg the line hugs the main). Every other answer - natural,
     * middle, sparta, breach, base - stages forward of the main.
     */
    private static boolean isMainChokeAnswer(HasPosition focus) {
        try {
            return focus instanceof atlantis.battle.advance.focus.AFocusPoint
                && ((atlantis.battle.advance.focus.AFocusPoint) focus).name().contains("MainChoke");
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static HasPosition attackAnchor() {
        return AttackFocusPoint.focusPoint();
    }
}
