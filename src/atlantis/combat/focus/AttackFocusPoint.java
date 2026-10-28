package atlantis.combat.focus;

import atlantis.battle.cluster.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Where the army goes on ATTACK (NEXT #194, 02_COMBAT Squad target).
 * Mirror of the defend side: the decision has one named home instead of
 * an inline chain in the anchor, so future attack reasons (contain,
 * push, expand) land here, never in the caller (CONVENTIONS §31).
 *
 * <p>The branch order lives in {@link AttackDecider} (pure, StarEngine-
 * pinned): known enemy base, else known enemy main, else the nearest
 * unexplored start, else our own main (which may itself be null on a
 * baseless map). This class only builds the snapshot from live game
 * state and twins the decision when it changes - the ATTACK_DECISION
 * row carries the target and the reason, so a game log names WHY the
 * army marches instead of inferring it from orders.</p>
 *
 * <p>Deliberately raw positions, never wrapped in a named focus object:
 * the focus constructors snap coordinates to walkable tiles and the
 * sim cannot observe the shift (its map is walkable everywhere), so a
 * wrapped anchor would be an unpinnable behavior change. Naming rides
 * the twin, not the anchor value.</p>
 */
public final class AttackFocusPoint {

    private static String lastTargetKey = null;
    private static AttackReason lastReason = null;

    private AttackFocusPoint() {
    }

    /**
     * The attack ground, or null when nothing qualifies.
     */
    public static HasPosition focusPoint() {
        AUnit base = EnemyUnits.enemyBase();
        HasPosition main = EnemyUnits.enemyMainBase();

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

        AttackSnapshot snapshot = new AttackSnapshot(
            base == null ? null : base.position(),
            main,
            unexplored,
            Select.mainOrAnyBuilding());

        AttackDecision decision = AttackDecider.decide(snapshot);
        twinOnChange(decision);

        return decision.target();
    }

    /**
     * ATTACK_DECISION rows only when the decision changes - the same
     * target re-asked every frame is not a new decision.
     */
    private static void twinOnChange(AttackDecision decision) {
        HasPosition target = decision.target();
        String key = target == null
            ? "-"
            : target.tx() + "," + target.ty() + "@" + decision.reason().name();

        if (key.equals(lastTargetKey)) return;
        lastTargetKey = key;
        lastReason = decision.reason();

        APosition tile = target == null ? null : APosition.create(target.tx(), target.ty());
        EventLog.key(LogKey.ATTACK_DECISION)
            .param("tx", tile == null ? -1 : tile.tx())
            .param("ty", tile == null ? -1 : tile.ty())
            .param("reason", decision.reason().name())
            .log(A.now(), A.minSec());
    }

    /**
     * Per-game reset (see {@code OnGameStarted}): a latched decision must
     * not leak into the next game in one JVM.
     */
    public static void clear() {
        lastTargetKey = null;
        lastReason = null;
    }

    /**
     * Test seam: the last twinned reason, or null when no decision left
     * this JVM yet.
     */
    static AttackReason lastReasonForTests() {
        return lastReason;
    }
}
