package atlantis.combat.squad.squads.omega;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.OurBuildingUnderAttack;
import atlantis.units.select.Count;

/**
 * Omega is battle squad that ALWAYS DEFENDS the main base and natural.
 */
public class Omega extends Squad {
    protected static Omega omega = null;

    private Omega() {
        super("Omega", Missions.DEFEND);
        omega = this;
    }

    public static Omega get() {
        if (omega == null) omega = new Omega();

        return omega;
    }


    @Override
    public boolean shouldHaveThisSquad() {
        if (OurBuildingUnderAttack.get() != null) return true;

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
        if (OurBuildingUnderAttack.get() != null) return A.inRange(
            2,
            Count.ourCombatUnits() / 7,
            4
        );

        if (Count.ourCombatUnits() <= 35) return 1;

        return Math.max(
            1,
            Math.min(2, Count.ourCombatUnits() / 15)
        );
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
