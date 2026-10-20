package atlantis.battle.strategy;

/**
 * The army-wide approaches a unit can fight under. One enum value per
 * approach; the decisions live in their own classes, picked by
 * {@link BattleStrategies} from the detected setup - never by scattered
 * if-branches at the call sites.
 */
public enum BattleStrategy {
    /** Tiny baseless duel: kite off to range minus a step, hunt lost contact. */
    DUEL_KITE,
    /** Bait detached at a held choke: pull the enemy into the funnel. */
    SCOUT_BAIT,
    /** Choke ahead with the enemy near and no crushing superiority: hold, don't push. */
    CHOKE_HOLD,
    /** Everything else: the standing fight. */
    OPEN_FIELD
}
