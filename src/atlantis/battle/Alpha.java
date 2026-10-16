package atlantis.battle;

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
