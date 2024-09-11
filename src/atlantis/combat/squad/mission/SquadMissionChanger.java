package atlantis.combat.squad.mission;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class SquadMissionChanger {
    protected static AUnit leader;
    protected static Selection units;

    // =========================================================

    public static boolean changeSquadMissionIfNeeded(Squad squad) {
        if (A.isUms()) return false;
        if (squad.hasMostlyOffensiveRole()) return false;

        leader = squad.leader();
        units = squad.selection();

        if (leader == null) return false;

        if (ChangeSquadToDefend.shouldChangeToDefend(squad)) return true;
        else if (ChangeSquadToDefault.shouldChangeToDefault(squad)) return true;

        return false;
    }
}
