package atlantis.combat.cluster;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Corsair cluster membership (NEXT #201): solo by default, with the army
 * only when threatening air exists. A Corsair sitting in the main cluster
 * inherits the army's anchor and trails it; on its own cluster it hunts
 * (Overlords vs Zerg, lone Wraiths vs Terran) instead of waiting for
 * targets to wander into the army's range.
 *
 * <p>Threatening air is any visible enemy air combat unit that is not a
 * Wraith, plus numerous KNOWN Wraiths - a lone Wraith is a duel the
 * Corsair takes alone, numerous Wraiths focus it down and it needs the
 * army's Dragoon cover. Known, not just visible: fogged Wraiths are
 * still out there, and flying solo into them is how Corsairs die
 * off-screen. Nothing reads this yet (sim-first): the cluster split in
 * {@code Alpha.gather} rides on top once the rule is pinned.</p>
 */
public final class CorsairCluster {

    /**
     * Numerous Wraiths (owner 2026-10-26, NEXT #201): at this many KNOWN
     * Wraiths the Corsairs merge with the army for Dragoon cover - below
     * it they duel or dodge alone. A named constant with a pinned
     * boundary (2 flies solo, 3 merges), not a bare literal, so the next
     * tuning has a name to aim at.
     */
    public static final int NUMEROUS_WRAITHS = 3;

    private CorsairCluster() {
    }

    /**
     * Live: this frame, does a Corsair belong with the army? Null-safe on
     * the world - an unreadable game flies solo, which is today's shape
     * anyway (every Corsair already sits in Alpha until the split).
     */
    public static boolean mergeWithArmy() {
        try {
            Selection air = Select.enemy().combatUnits().air();
            int visibleAir = air == null ? 0 : air.count();
            int visibleWraiths = Select.enemy().ofType(AUnitType.Terran_Wraith).count();
            int otherAir = Math.max(0, visibleAir - visibleWraiths);

            return mergeWithArmy(otherAir, EnemyUnits.count(AUnitType.Terran_Wraith));
        }
        catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * The owner's condition on numbers alone: any non-Wraith air combat
     * merges (Mutalisks never fly alone, and a Scout means air play is
     * coming), Wraiths merge only when numerous.
     */
    public static boolean mergeWithArmy(int otherAirCombat, int knownWraiths) {
        return otherAirCombat > 0 || knownWraiths >= NUMEROUS_WRAITHS;
    }
}
