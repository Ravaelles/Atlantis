package atlantis.combat.strategy;

import atlantis.map.choke.AChoke;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

/**
 * The detected setup every approach decides on: duel scale, the choke on
 * the way, who stands near it, and whether we crush. Map reads are
 * defensive (no map data in tests) - a missing choke is null, never an
 * exception.
 */
public final class BattleSetup {

    private BattleSetup() {
    }

    public static boolean isDuel() {
        return Count.bases() == 0 && combatants() <= DuelKite.MAX_DUEL_COMBATANTS;
    }

    public static int combatants() {
        int ours = Select.ourCombatUnits() == null ? 0 : Select.ourCombatUnits().count();
        int theirs = Select.enemyCombatUnits() == null ? 0 : Select.enemyCombatUnits().count();

        return ours + theirs;
    }

    /** The choke on this unit's way, or null when there is none (or no map data). */
    public static AChoke chokeNear(AUnit unit) {
        if (unit == null) return null;

        try {
            AChoke choke = unit.nearestChoke();
            if (choke == null || choke.center() == null) return null;
            if (unit.distTo(choke.center()) > ChokeStandoff.DETECT_TILES) return null;

            return choke;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static int enemiesNear(HasPosition point, double tiles) {
        try {
            return Select.enemy().combatUnits().inRadius(tiles, point).count();
        } catch (Exception ignored) {
            return 0;
        }
    }

    public static int oursNear(HasPosition point, double tiles) {
        try {
            return Select.ourCombatUnits().inRadius(tiles, point).count();
        } catch (Exception ignored) {
            return 0;
        }
    }
}
