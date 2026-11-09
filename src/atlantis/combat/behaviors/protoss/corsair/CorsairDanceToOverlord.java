package atlantis.combat.behaviors.protoss.corsair;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Corsair dance (owner 2026-11-09, port of the old
 * {@code CorsairDanceToOverlord}): a Corsair mid-duel with an Overlord
 * closes to firing distance while cooling and shoots on ready - the
 * old pathing kept the duel at range instead of face-tanking the
 * Spore math behind it.
 */
public final class CorsairDanceToOverlord {

    /**
     * Guns at or above this cooldown dance in instead of trading.
     */
    static final int DANCE_COOLDOWN = 9;

    private CorsairDanceToOverlord() {
    }

    public static boolean applies(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive() || !unit.isCorsair()) return false;

            AUnit target = unit.target();
            if (target == null || !target.isAlive() || !target.isOverlord()) return false;
            if (unit.cooldown() < DANCE_COOLDOWN) return false;

            return unit.distTo(target) >= 3 + unit.woundPercent() / 50.0;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    public static boolean act(AUnit unit) {
        if (!applies(unit)) return false;

        AUnit target = unit.target();

        unit.setTooltipTactical("CorsairDance2Over");

        return unit.move(target, Actions.MOVE_DANCE_TO);
    }
}
