package atlantis.combat.strategy;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

/**
 * The decider: which approach this unit fights under, from the detected
 * setup. One place, priority-ordered, most specific first - call sites
 * branch on the answer instead of re-spelling the conditions. New
 * approaches add one enum value, one class, one line here.
 */
public final class BattleStrategies {

    private BattleStrategies() {
    }

    public static BattleStrategy pick(AUnit unit, HasPosition anchor) {
        if (unit == null) return BattleStrategy.OPEN_FIELD;
        // Home-choke breakout (owner 2026-10-29): at 150%+ strength with
        // fewer than 2 bases the line never holds its own main or natural
        // choke - the choke strategies park a finished army at home
        // instead of marching out. The bait and the duel are not choke
        // strategies, they evaluate below untouched.
        boolean breakout = breakoutFromHomeChoke(unit);
        if (!breakout && ChokeCollapse.applies(unit, anchor)) return BattleStrategy.COLLAPSE;
        if (SquadScout.applies(unit, anchor)) return BattleStrategy.SCOUT_BAIT;
        if (!breakout && ChokeStandoff.applies(unit, anchor)) return BattleStrategy.CHOKE_HOLD;
        if (DuelKite.applies(unit)) return BattleStrategy.DUEL_KITE;

        return BattleStrategy.OPEN_FIELD;
    }

    /**
     * At or above this relative strength a one-base army breaks out
     * through its own main and natural chokes instead of holding them
     * (owner 2026-10-29). Below it, or with 2+ bases, the choke
     * strategies own those chokes as before.
     */
    public static final int BREAKOUT_STRENGTH = 150;

    /**
     * True when this unit stands at our main or natural choke with a
     * one-base army at 150%+ strength: the choke strategies must not own
     * it. The choke near the unit decides, not the anchor - the unit
     * holds where it stands. Public as a seam, pinned without a game.
     */
    public static boolean breakoutFromHomeChoke(AUnit unit) {
        try {
            if (unit == null || !unit.isAlive()) return false;
            if (atlantis.units.select.Count.bases() >= 2) return false;
            if (atlantis.information.generic.Army.strength() < BREAKOUT_STRENGTH) return false;

            atlantis.map.choke.AChoke choke = BattleSetup.chokeNear(unit);
            if (choke == null) return false;

            // Evaluated separately: a failing main-choke read must not
            // hide a natural-choke hit behind one shared expression.
            try {
                if (choke.isMainChoke()) return true;
            } catch (Exception ignored) {
            }
            try {
                return choke.isNaturalChoke();
            } catch (Exception ignored) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }
}
