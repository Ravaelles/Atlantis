package atlantis.combat.strategy;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Choke collapse (NEXT #170): the bait pulled the enemy into the funnel
 * and the line holds crushing superiority - so the line joins the bait
 * at the funnel mouth instead of standing the standoff. Same trigger
 * shape as the bait, mirrored: contested choke, formed line, but
 * crushing instead of waiting. The collapse point IS the bait point
 * (two tiles past the center toward the enemy); the bait converges
 * with the line instead of being left out alone.
 */
public final class ChokeCollapse {

    private ChokeCollapse() {
    }

    public static boolean applies(AUnit unit, HasPosition anchor) {
        if (unit == null) return false;
        if (Count.bases() == 0) return false;

        AChoke choke = BattleSetup.chokeNear(unit);
        if (choke == null || choke.center() == null) return false;
        if (BattleSetup.combatants() < 3) return false;
        if (BattleSetup.enemiesNear(choke.center(), ChokeStandoff.CONTEST_TILES) == 0) return false;
        if (!ChokeStandoff.crushing(choke)) return false;

        Selection ours = Select.ourCombatUnits();
        if (ours == null || ours.count() < 3) return false;

        return SquadScout.pickBait(ours) != null;
    }

    /** Guarded detection: the collapse point for this unit, or null when there is no crush. */
    public static APosition collapsePointFor(AUnit unit) {
        try {
            AChoke choke = BattleSetup.chokeNear(unit);
            if (choke == null || choke.center() == null) return null;

            AUnit foe = Select.enemy().combatUnits().nearestTo(choke.center());
            if (foe == null || foe.position() == null) return null;

            return SquadScout.baitPoint(choke.center(), foe.position());
        } catch (Exception ignored) {
            return null;
        }
    }
}
