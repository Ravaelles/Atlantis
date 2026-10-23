package atlantis.battle.retreating;

import atlantis.battle.cluster.Alpha;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * A unit cut off from the army with enemies on it never marches to the
 * far rally through the pack (owner 2026-10-26, tested loss vs Protoss:
 * fresh dragoons marched into a dozen zealots without fighting once and
 * died on the way to a rally 15 tiles away). If it can hit back it
 * fights instead; otherwise it steps away locally instead of donating
 * the march. Units with the army keep the coordinated retreat - only
 * the detached ones decide for themselves.
 */
public final class CutoffRetreat {

    private CutoffRetreat() {
    }

    /** Detached from the army: beyond mutual-support range of its center. */
    private static final double DETACHED_ARMY_TILES = 12;

    /** Enemies only count when close enough to matter for the march. */
    private static final double DETACHED_THREAT_TILES = 10;

    /**
     * The enemy this detached unit must answer, or null when the unit
     * should keep the shared retreat: with the army, or with nobody
     * close enough to intercept the march home.
     */
    public static AUnit threatIfCutOff(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;

        HasPosition center = Alpha.get().center();
        if (center == null) return null;
        if (unit.distTo(center) < DETACHED_ARMY_TILES) return null;

        AUnit threat = nearestEnemyCombatUnit(unit);
        if (threat == null || !threat.isAlive()) return null;
        if (unit.distTo(threat) > DETACHED_THREAT_TILES) return null;

        return threat;
    }

    private static AUnit nearestEnemyCombatUnit(AUnit unit) {
        try {
            return Select.enemy().combatUnits().nearestTo(unit);
        }
        catch (RuntimeException ignored) {
            return null;
        }
    }
}
