package atlantis.combat.squad.mission;

import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ChangeSquadMission {
    
    protected static AUnit unit;
    protected static Selection units;

    // =========================================================

    public static boolean changeSquadMissionIfNeeded(Squad squad) {
        unit = squad.centerUnit();
        units = squad.selection();

        if (unit == null) {
            return false;
        }

        if (ChangeSquadToDefend.shouldChangeToDefend(squad)) {
            return true;
        }
        else if (ChangeSquadToDefault.shouldChangeToDefault(squad)) {
            return true;
        }

        return false;
    }

    // =========================================================

}