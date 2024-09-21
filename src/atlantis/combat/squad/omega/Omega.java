package atlantis.combat.squad.omega;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.select.Count;

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
        if (EnemyWhoBreachedBase.numberOfAttacksOnBase() > 0) return true;

//        return false;
        return (A.supplyUsed(60) || (Missions.isGlobalMissionAttack() && Count.ourCombatUnits() >= 18));
    }

    // =========================================================

    @Override
    public int expectedUnits() {
        return Math.max(
            1,
            Math.min(3, (int) (Count.ourCombatUnits() / 9))
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
