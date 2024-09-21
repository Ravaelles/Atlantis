package atlantis.combat.squad.delta;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

/**
 * Delta is AIR FORCES battle squad. Only offensive air units belong here.
 */
public class Delta extends Squad {
    protected static Delta delta = null;

    // =========================================================

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
    public boolean shouldHaveThisSquad() {
        return Select.ourCombatUnits().air().havingWeapon().count() >= 1;
    }

    // =========================================================

    @Override
    public Mission mission() {
        return Missions.ATTACK;
    }

    @Override
    public int expectedUnits() {
        return Math.max(
            1,
            Math.min(5, Count.ourCombatUnits() / 8)
        );
    }

    public static int count() {
        return get().size();
    }
}
