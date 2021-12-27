package atlantis.combat.squad.beta;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.units.select.Count;

/**
 * Beta is battle squad that ALWAYS DEFENDS the main base and natural.
 */
public class Beta extends Squad {

    protected static Beta beta = null;

    private Beta() {
        super("Beta", Missions.DEFEND);
        beta = this;
    }

    public static Beta get() {
        if (beta == null) {
            beta = new Beta();
        }

        return beta;
    }

    // =========================================================

    @Override
    public int expectedUnits() {
        return Math.max(
                1,
                Math.min(5, Count.ourCombatUnits() / 12)
        );
    }

    public static int count() {
        return get().size();
    }

}
