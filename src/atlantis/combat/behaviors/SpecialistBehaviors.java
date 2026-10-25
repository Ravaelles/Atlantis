package atlantis.combat.behaviors;

import atlantis.units.AUnit;

/**
 * Per-unit specialist behaviors (NEXT #198): Corsairs hunt, Reavers brew
 * scarabs, Dark Templar break contact, High Templar storm. One small
 * class per behavior with a single gate, tried in this explicit order;
 * the first one that issues an order owns the frame. Types not listed
 * here fall through untouched - the generic pipeline decides for them.
 *
 * <p>The order is survival before damage: a behavior never pulls a unit
 * out of a retreat the shared ladder ordered (the hook sits past
 * retreat in {@code BattleCommander.act}).</p>
 */
public final class SpecialistBehaviors {

    private SpecialistBehaviors() {
    }

    /**
     * True when a specialist behavior issued an order and owns the frame.
     * Pure dispatch: every behavior decides for itself, this only fixes
     * the order (CONVENTIONS §31: one list, visible, pinned by the
     * per-behavior tests, not by an ordering accident).
     */
    public static boolean act(AUnit unit) {
        if (unit == null || !unit.isAlive()) return false;

        if (CorsairHunt.act(unit)) return true;
        if (ProduceScarabs.act(unit)) return true;

        // DarkTemplarRunBehavior + HighTemplarStormBehavior register here
        // in the same one-line shape (NEXT #198, slice 2).

        return false;
    }
}
