package atlantis.battle.strategy;

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
        if (ChokeCollapse.applies(unit, anchor)) return BattleStrategy.COLLAPSE;
        if (SquadScout.applies(unit, anchor)) return BattleStrategy.SCOUT_BAIT;
        if (ChokeStandoff.applies(unit, anchor)) return BattleStrategy.CHOKE_HOLD;
        if (DuelKite.applies(unit)) return BattleStrategy.DUEL_KITE;

        return BattleStrategy.OPEN_FIELD;
    }
}
