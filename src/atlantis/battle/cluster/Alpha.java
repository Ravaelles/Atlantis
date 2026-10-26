package atlantis.battle.cluster;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

/**
 * The main army squad. Phase 0 of the combat cutover
 * (_AI/__PLAN-COMBAT.md): assignment is deleted, so the roster is empty
 * and the center falls back to the main base - the army rallies at home
 * until BattleCommander owns the cluster (step 5). Exists so the dozens
 * of army-shape reads keep compiling and answering sanely.
 */
public class Alpha {
    private static final Squad ALPHA = new Squad("Alpha");

    private Alpha() {
    }

    public static Squad get() {
        return ALPHA;
    }

    /**
     * Roster fill: every squadless combat unit joins the cluster. Runs
     * once per frame from the fight composition before anything reads
     * the roster.
     *
     * <p>One exception (NEXT #201): a solo Corsair does not join - it
     * hunts on its own (CorsairHunt) instead of trailing the army, and
     * merges only when the CorsairCluster rule says threatening air
     * needs Dragoon cover.</p>
     */
    public static void gather() {
        atlantis.units.select.Selection army = Select.ourCombatUnits();
        if (army == null) return;

        for (AUnit unit : army.list()) {
            if (unit != null && unit.isAlive() && unit.squad() == null) {
                if (unit.isCorsair()
                    && !atlantis.combat.cluster.CorsairCluster.mergeWithArmy()) continue;

                ALPHA.addUnit(unit);
            }
        }
    }

    public static int count() {
        return ALPHA.size();
    }

    public static AUnit alphaLeader() {
        return ALPHA.leader();
    }

    public static atlantis.map.position.HasPosition alphaCenter() {
        if (!ALPHA.isEmpty() && ALPHA.center() != null) return ALPHA.center();

        AUnit main = Select.mainOrAnyBuilding();

        return main;
    }
}
