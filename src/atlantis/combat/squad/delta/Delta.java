package atlantis.combat.squad.delta;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.beta.Beta;
import atlantis.units.select.Count;

/**
 * Delta is AIR FORCES battle squad. Only offensive air units belong here.
 */
public class Delta extends Squad {

    protected static Delta delta = null;

    private Delta() {
        super("Delta", Missions.ATTACK);
        delta = this;
    }

    public static Delta get() {
        if (delta == null) {
            delta = new Delta();
        }

        return delta;
    }

    // =========================================================

    @Override
    public int expectedUnits() {
        return Math.max(
                1,
                Math.min(5, Count.ourCombatUnits() / 8)
        );
    }
}
