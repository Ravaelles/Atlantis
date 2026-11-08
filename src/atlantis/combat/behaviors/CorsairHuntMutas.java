package atlantis.combat.behaviors;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Corsairs hunt Mutalisks (owner 2026-11-09, port of the old
 * {@code CorsairHuntMutas}): healthy Corsairs answer muta packs
 * near the base first, then the nearest known one. Dark Archon food
 * rules still apply elsewhere - this only names the duel.
 */
public final class CorsairHuntMutas {

    /**
     * Below this hp a Corsair does not trade with Mutalisks.
     */
    static final int MIN_HP = 42;

    private CorsairHuntMutas() {
    }

    public static boolean applies(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive() || !unit.isCorsair()) return false;
            if (!Enemy.zerg()) return false;
            if (unit.hp() <= MIN_HP) return false;

            return muta(unit) != null;
        } catch (Exception | Error ignored) {
            return false;
        }
    }

    public static boolean act(AUnit unit) {
        AUnit muta = applies(unit) ? muta(unit) : null;
        if (muta == null) return false;

        unit.setTooltipTactical("CorsairHuntMuta");

        return unit.attackUnit(muta);
    }

    static AUnit muta(AUnit unit) {
        try {
            Selection mutalisks = Select.enemyCombatUnits().mutalisks().havingPosition();
            if (mutalisks.isEmpty()) return null;

            Selection close = mutalisks.inRadius(6, unit);
            if (close.notEmpty()) {
                return close.mostWoundedOrNearest(unit);
            }

            AUnit nearBase = mutalisks.nearestTo(Select.mainOrAnyBuilding());
            if (nearBase != null) return nearBase;

            return mutalisks.nearestTo(unit);
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
