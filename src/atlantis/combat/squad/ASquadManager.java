package atlantis.combat.squad;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.mission.SquadMissionChanger;
import atlantis.game.A;
import atlantis.units.AUnit;

import java.util.ArrayList;

/**
 * Commands all existing battle squads.
 */
public class ASquadManager {

    /** All squads. "Alpha" - main army. After some time "Beta" - always defend main + natural. */
    protected static ArrayList<Squad> squads = new ArrayList<>();

    // =========================================================
    // Manage squads

    /**
     * Acts with all units that are part of given battle squad, according to the SquadMission object and using
     * proper micro managers.
     */
    public static void update(Squad squad) {
        if (A.everyNthGameFrame(11)) {
            SquadMissionChanger.changeSquadMissionIfNeeded(squad);
        }

        // Act with every combat unit
        for (AUnit unit : squad.list()) {
            CombatUnitManager.update();
        }
    }
}
