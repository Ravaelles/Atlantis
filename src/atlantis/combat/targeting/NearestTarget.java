package atlantis.combat.targeting;

import atlantis.units.AUnit;

/**
 * Nearest engageable enemy (ladder rule (8)): whoever is closest and
 * damageable, focus bans notwithstanding. This is the rule that stops
 * the march for chaff - lings and workers, which rules 3 and 5 refuse
 * by design, still deserve a fight instead of a walk-past. Split out
 * as a named rule, ordered visibly in {@link TargetLadder}: one
 * behavior, one gate, the 9-tile envelope as the ladder's literal.
 */
public final class NearestTarget {

    /**
     * Engagement envelope: farther foes do not stop the march.
     */
    public static final double NEAREST_TILES = 9;

    private NearestTarget() {
    }

    public static AUnit pick(AUnit unit) {
        if (unit == null) return null;

        try {
            return ASquadTargeting.nearestEngageableEnemy(unit, NEAREST_TILES);
        } catch (Exception | Error ignored) {
            return null;
        }
    }
}
