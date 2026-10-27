package atlantis.combat.behaviors;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

/**
 * Dark Templar break contact when hurt (NEXT #198, port of the old
 * DarkTemplarRunWhenAttacked gate): a DT that takes damage is detected
 * (or about to be) and dies fast in a trade, so it runs instead of
 * fighting like a zealot. An unhurt DT keeps killing - the gate is the
 * contact, not the presence - and threats that cannot hit it never
 * chase it off its cloak.
 *
 * <p>Numbers carry legacy provenance: the under-attack window (150) and
 * the threat radius (5) are the old gate's, not tuned here.</p>
 */
public final class RunWhenAttacked {

    private static final int UNDER_ATTACK_WINDOW = 30 * 5;
    private static final double THREAT_RADIUS = 5;

    private RunWhenAttacked() {
    }

    /**
     * Only hurt Dark Templar with something nearby that can hit them.
     */
    public static boolean applies(AUnit unit) {
        if (unit == null || !unit.isAlive()) return false;
        if (!unit.isDarkTemplar()) return false;
        if (!unit.lastUnderAttackLessThanAgo(UNDER_ATTACK_WINDOW)) return false;

        return !threats(unit).isEmpty();
    }

    /**
     * Break away from the nearest threat. True when an order went out.
     */
    public static boolean act(AUnit unit) {
        Selection threats = applies(unit) ? threats(unit) : null;
        if (threats == null || threats.isEmpty()) return false;

        AUnit threat = threats.nearestTo(unit);
        if (threat == null) return false;

        return unit.moveAwayFrom(threat, 6, Actions.RUN_ENEMY, "BreakContact");
    }

    /**
     * Enemies close enough to hit this unit - the only run worth taking.
     */
    static Selection threats(AUnit unit) {
        return unit.enemiesThatCanAttackMe(THREAT_RADIUS);
    }
}
