package atlantis.combat.squad.squads.x;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

/**
 * X is a special squad for Dark Templars / special offensive units that can operate alone.
 */
public class X extends Squad {
    protected static X x = null;

    // =========================================================

    private X() {
        super("X", Missions.ATTACK);
        x = this;
    }

    public static X get() {
        if (x == null) {
            x = new X();
        }

        return x;
    }

    // =========================================================

    @Override
    public boolean shouldHaveThisSquad() {
        return We.protoss();
    }

    // =========================================================

    @Override
    public boolean hasMostlyOffensiveRole() {
        return true;
    }

    @Override
    public Mission mission() {
        return Missions.ATTACK;
    }

    @Override
    public int expectedUnits() {
        return 2;
    }

    public static int count() {
        return get().size();
    }
}
