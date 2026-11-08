package atlantis.combat.behaviors;

import atlantis.game.A;
import atlantis.game.ARandom;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Anti-idle relocation (owner 2026-11-09, port of the old
 * {@code CorsairChangeLocationIfRanTooLong}): a Corsair that fired
 * nothing for a long while goes elsewhere instead of holding the
 * #NO_COMMAND pose forever. Only when there is nothing to shoot -
 * the hunt legs above own every frame a target exists.
 */
public final class CorsairRelocateWhenIdle {

    private CorsairRelocateWhenIdle() {
    }

    public static boolean applies(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive() || !unit.isCorsair()) return false;
            if (unit.isGroundUnit()) return false;
            // Throttle, not a starter pistol (owner 2026-11-09): a
            // relocation went out recently, let it play instead of
            // spamming a new one every frame. Units that never relocated
            // fall through to the anti-idle rule below instead of
            // standing on #NO_COMMAND forever.
            if (unit.lastActionLessThanAgo(30 * 8, Actions.SPECIAL)) return false;
            if (unit.enemiesNear().air().notEmpty()) return false;

            if (unit.hp() <= 95 && unit.lastUnderAttackLessThanAgo(30 * 6)) return true;
            if (unit.enemiesNear().nonBuildings().canBeAttackedBy(unit, 0.95).notEmpty()) return false;
            if (unit.didntShootRecently(7)) return true;
            if (unit.enemiesNear().inRadius(14, unit).empty()) return false;

            return unit.lastActionMoreThanAgo(3) || unit.lastAttackFrameMoreThanAgo(30 * 4);
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    public static boolean act(AUnit unit) {
        if (!applies(unit)) return false;

        if (A.seconds() % 14 >= 5) {
            APosition invisible = AMap.randomInvisiblePosition(unit);
            if (invisible != null) {
                unit.setTooltipTactical("ChangeLocRand");

                return unit.move(invisible, Actions.SPECIAL, "ChangeLocRand", true);
            }
        }

        int delta = 12 + unit.id() % 7;
        int dx = -delta + ARandom.rand(0, 2 * delta);
        int dy = -delta + ARandom.rand(0, 2 * delta);
        APosition goTo = unit.position().translateByTiles(dx, dy).makeValidGroundPosition();
        if (goTo == null) return false;

        unit.setTooltipTactical("BeSmart");

        return unit.move(goTo, Actions.SPECIAL, "BeSmart");
    }
}
