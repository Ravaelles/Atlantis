package atlantis.combat.squad.alpha;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;

/**
 * Alpha is battle squad that is the main battle squad. Most new units arrive here (handled by NewUnitsToSquadsAssigner)
 * and can be later transferred to other squads if needed (via ASquadManager).
 */
public class Alpha extends Squad {

    protected static Alpha alpha = null;

    private Alpha() {
        super("Alpha", Missions.initialMission());
    }

    /**
     * Get first, main squad of units.
     */
    public static Alpha get() {
        if (alpha == null) {
            alpha = new Alpha();
        }

        return alpha;
    }

    // =========================================================

    @Override
    public boolean shouldHaveThisSquad() {
        return true;
    }

    @Override
    public boolean allowsSideQuests() {
        return A.s <= 60 * 7;
//        return count() >= 14;
    }

    @Override
    public Mission mission() {
        if (A.isUms()) return Missions.ATTACK;

        return super.mission();
    }

    public static AUnit alphaLeader() {
        Alpha alpha = get();
        if (alpha == null) return null;

        return alpha.leader();
    }

    // =========================================================

    public int expectedUnits() {
        return 0;
    }

    public static int count() {
        return get().size();
    }

}
