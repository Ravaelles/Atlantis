package atlantis.combat.squad.squads.iota;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.information.enemy.EnemyOnCloseIsland;

/**
 * Iota is a special battle squad that is used for ISLAND DROPS - I(sland).
 */
public class Iota extends Squad {
    protected static Iota Iota = null;

    private Iota() {
        super("Iota", Missions.ATTACK);
        Iota = this;
    }

    public static Iota get() {
        if (Iota == null) Iota = new Iota();

        return Iota;
    }


    @Override
    public boolean shouldHaveThisSquad() {
        return true;
//        return EnemyOnCloseIsland.get() != null
//            || lastUnderAttackLessThanAgo(30 * 4);
    }

    // =========================================================

    @Override
    public boolean hasMostlyOffensiveRole() {
        return true;
    }

    @Override
    public int expectedUnits() {
        return 6;
    }

    @Override
    public Mission mission() {
        return Missions.ATTACK;
    }

    public static int count() {
        return get().size();
    }

    @Override
    public void handleReinforcements() {
    }
}
