package atlantis.combat.cluster;

import atlantis.battle.kite.CombatTuning;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * Breach rules (extracted from BattleCommander): raiders in the base
 * force the cluster to FIGHT, name the fallback target when nothing
 * else is in reach, and own the choke-side rally. One rule family,
 * one home - the commander only calls it.
 */
public final class ClusterBreach {

    private ClusterBreach() {
    }


    /**
     * The breach half of the tactic gate: raiders in the base force FIGHT
     * before any evaluation runs. Workers included, deliberately wider
     * than {@code EnemyUnitBreachedBase} (measured: both its paths filter
     * on combat units, so two worker-raiders chewing the base read as no
     * breach - exactly the flee this rule exists to forbid). Radius 8 is
     * the detector's own close-to-base radius; the shared detector stays
     * untouched (its other readers must not start firing on scouts).
     * Public as a seam: the rule pins in a stub state without running
     * the frame.
     */
    public static boolean breachForcesFight() {
        if (atlantis.information.enemy.EnemyUnitBreachedBase.someone()) return true;

        AUnit main = HomeGround.resolve().main();
        if (main == null) return false;

        AUnit foe = main.enemiesNear().nearestTo(main);

        return foe != null && foe.distTo(main) <= 8;
    }
    /**
     * Who a unit with no enemy in reach attacks when the base is breached:
     * without this a DEFEND cluster whose 9-tile reach misses the raiders
     * chewing our workers just bunches on the anchor while the base dies.
     * Public as a seam: the rule pins in a stub state without a game.
     *
     * <p>
     * // @ToDo: replace with a proper approach - threat-aware target
     * assignment owned by the cluster (which enemy, which responders,
     * what the rest holds), instead of this per-unit fallback. Ad-hoc
     * so DEFEND units answer base attacks at all. Race-condition note:
     * cluster/base/enemy positions are read across frames, so the anchor
     * can move under us between the 30-tile check and the attack order;
     * acceptable for a fallback, the proper approach must own the
     * assignment instead of racing it. Candidate bounding (today: any
     * armed enemy, which can still be a far scout when the breach is
     * elsewhere) belongs to the same rewrite.
     * </p>
     */
    public static AUnit breachedBaseTarget(AUnit unit) {
        if (unit == null || !unit.isAlive()) return null;

        AUnit breached = atlantis.information.enemy.EnemyUnitBreachedBase.get();
        if (breached == null || !breached.isAlive()) return null;
        if (!breached.isDetected()) return null;

        atlantis.units.select.Selection enemies =
            atlantis.units.select.Select.enemy().combatUnits().havingWeapon();
        if (enemies.isEmpty()) return breached;

        AUnit base = atlantis.units.select.Select.ourBases().nearestTo(unit);
        AUnit foe;
        if (base != null && unit.distTo(base) <= 30) {
            foe = enemies.nearestTo(unit);
        }
        else {
            AUnit main = atlantis.units.select.Select.ourBases()
                .nearestTo(atlantis.information.generic.InitialMainPosition.initialMainPosition());
            foe = main == null ? breached : enemies.nearestTo(main);
        }
        if (foe == null) return breached;

        if (!foe.isDetected()) return null;

        return unit.hasWeaponToAttackThisUnit(foe) ? foe : null;
    }
    /**
     * Where a withdrawing unit rallies (owner): three tiles from the main
     * toward the main choke - a forward defensive spot, not the mineral
     * line (retreating into the miners donates the base, and the old
     * MissionDefendFocusPoint played exactly this role). Null-safe: no
     * main, no choke, or an unhostable point falls back to the defend
     * anchor, and a null anchor falls through to fighting in place.
     * Public as a seam: the rally pins in a stub state without a game.
     */
    public static HasPosition breachRallyPoint() {
        HomeGround home = HomeGround.resolve();
        if (!home.isComplete()) return ClusterAnchor.defendAnchor();

        AUnit main = home.main();

        try {
            APosition point = main.position()
                .translateTilesTowards(CombatTuning.current().getBreachRallyTiles(), home.mainChoke().center());
            if (point == null) return ClusterAnchor.defendAnchor();

            return point;
        }
        catch (RuntimeException ignored) {
            return ClusterAnchor.defendAnchor();
        }
    }
}
