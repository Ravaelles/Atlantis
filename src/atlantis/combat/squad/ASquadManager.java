package atlantis.combat.squad;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.squad.mission.SquadMissionChanger;
import atlantis.game.A;
import atlantis.units.AUnit;

/**
 * Commands all existing battle squads.
 */
public class ASquadManager {

    private Squad squad;

    public ASquadManager(Squad squad) {
        this.squad = squad;
    }

    /**
     * Acts with all units that are part of given battle squad.
     */
    public void update() {
        if (A.everyNthGameFrame(11)) {
            SquadMissionChanger.changeSquadMissionIfNeeded(squad);
        }

        // Act with every combat unit
        for (AUnit unit : squad.units().list()) {
            (new CombatUnitManager(unit)).handle();
        }
    }
}
