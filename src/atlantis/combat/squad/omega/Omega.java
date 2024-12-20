package atlantis.combat.squad.omega;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.units.select.Count;
import atlantis.util.Enemy;

/**
 * Omega is battle squad that ALWAYS DEFENDS the main base and natural.
 */
public class Omega extends Squad {
    protected static Omega omega = null;

    private Omega() {
        super("Omega", Missions.DEFEND);
        omega = this;

        omega.setMission(Missions.DEFEND);
    }

    public static Omega get() {
        if (omega == null) omega = new Omega();

        return omega;
    }

    @Override
    public boolean shouldHaveThisSquad() {
        if (Count.cannons() > 0) return false;
        if (Count.ourCombatUnits() <= 18) return false;

        if (EnemyUnitBreachedBase.numberOfAttacksOnBase() > 0) return true;

//        return false;
        int minUnits = Enemy.zerg() ? 18 : (Enemy.protoss() ? 25 : 21);

        return (A.supplyUsed(70) || (Missions.isGlobalMissionAttack() && Count.ourCombatUnits() >= minUnits));
    }

    // =========================================================

    @Override
    public int expectedUnits() {
        return 1;

//        if (Count.ourCombatUnits() <= 35) return 1;
//
//        return Math.max(
//            1,
//            Math.min(2, Count.ourCombatUnits() / 15)
//        );
    }

    @Override
    public Mission mission() {
        return Missions.DEFEND;
    }

    public static int count() {
        return get().size();
    }

    @Override
    public void handleReinforcements() {
        (new OmegaReinforcements(this)).handleReinforcements();
    }
}
