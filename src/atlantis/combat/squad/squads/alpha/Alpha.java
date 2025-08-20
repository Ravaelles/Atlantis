package atlantis.combat.squad.squads.alpha;

import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.squads.bravo.ShouldHaveBravo;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

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

    public static double eval() {
        AUnit unit = alphaLeader();
        if (unit == null) return 0;

        return unit.eval();
    }

    public static double groundDistToMain() {
        AUnit unit = alphaLeader();
        if (unit == null) return 0;

        return unit.groundDistToMain();
    }

    // =========================================================

    @Override
    public boolean shouldHaveThisSquad() {
        return true;
    }

    @Override
    public boolean allowsSideQuests() {
        return !ShouldHaveBravo.shouldHave();
//        return A.s <= 60 * 7;
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

    public static HasPosition alphaLeaderOrAnyUnit() {
        AUnit leader = alphaLeader();
        if (leader != null) return leader;

        return Select.mainOrAnyBuilding();
    }

    // =========================================================

    public int expectedUnits() {
        return 0;
    }

    public static int count() {
        return get().size();
    }

}
