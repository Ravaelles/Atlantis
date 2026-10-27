package atlantis.combat.strategy;

import atlantis.combat.Behavior;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

/**
 * The army-wide approaches a unit can fight under. One enum value per
 * approach; the decisions live in their own classes, picked by
 * {@link BattleStrategies} from the detected setup - never by scattered
 * if-branches at the call sites.
 * <p>
 * The same holds for the no-contact regroup point below: it is answered
 * here, per value, so callers never switch on the enum. Values without a
 * strategy point (duel, open field) answer null and the caller falls back
 * to the formation.
 */
public enum BattleStrategy {
    /**
     * Tiny baseless duel: kite off to range minus a step, hunt lost contact.
     */
    DUEL_KITE,
    /**
     * Bait detached at a held choke: pull the enemy into the funnel.
     */
    SCOUT_BAIT,
    /**
     * Choke ahead with the enemy near and no crushing superiority: hold, don't push.
     */
    CHOKE_HOLD,
    /**
     * Bait pulled the enemy into the funnel and the line crushes: join the bait at the mouth.
     */
    COLLAPSE,
    /**
     * Everything else: the standing fight.
     */
    OPEN_FIELD;

    /**
     * Where this approach regroups with no enemy in reach, or null when it
     * has no point and the formation fallback owns the unit.
     */
    public APosition regroupPointFor(AUnit unit) {
        switch (this) {
            case COLLAPSE:
                return ChokeCollapse.collapsePointFor(unit);

            case SCOUT_BAIT:
                return SquadScout.baitPointFor(unit);

            case CHOKE_HOLD:
                return ChokeStandoff.holdPointFor(unit);

            default:
                return null;
        }
    }

    /**
     * Move behavior for the regroup point above. Meaningful only when
     * {@link #regroupPointFor} answers non-null.
     */
    public Behavior regroupTag() {
        switch (this) {
            case COLLAPSE:
                return Behavior.COLLAPSE;

            case SCOUT_BAIT:
                return Behavior.BAIT;

            case CHOKE_HOLD:
                return Behavior.STANDOFF;

            default:
                return Behavior.GATHER;
        }
    }
}
