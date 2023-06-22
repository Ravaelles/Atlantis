package atlantis.combat.squad.mission;

import atlantis.combat.missions.Missions;
import atlantis.combat.squad.Squad;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.We;

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
        else if (ChangeSquadToAttack.shouldChangeToAttack(squad)) {
            return true;
        }

        return false;
    }

    // =========================================================

}