package atlantis.combat.focus;

import atlantis.battle.cluster.Alpha;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Where the army goes on ATTACK (NEXT #194, 02_COMBAT Squad target).
 * Mirror of the defend side: the decision has one named home instead of
 * an inline chain in the anchor, so future attack reasons (contain,
 * push, expand) land here, never in the caller (CONVENTIONS §31).
 *
 * <p>Verbatim the old anchor logic, only relocated: known enemy base,
 * else known enemy main, else the nearest unexplored start, else our
 * own main (which may itself be null on a baseless map).</p>
 *
 * <p>Deliberately raw positions, never wrapped in a named focus object:
 * the focus constructors snap coordinates to walkable tiles and the
 * sim cannot observe the shift (its map is walkable everywhere), so a
 * wrapped anchor would be an unpinnable behavior change. Naming rides
 * the focus twin, not the anchor value.</p>
 */
public final class AttackFocusPoint {

    private AttackFocusPoint() {
    }

    /**
     * The attack ground, or null when nothing qualifies.
     */
    public static HasPosition focusPoint() {
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
