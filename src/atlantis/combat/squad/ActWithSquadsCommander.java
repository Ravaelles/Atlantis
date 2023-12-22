package atlantis.combat.squad;

import atlantis.architecture.Commander;
import atlantis.combat.squad.mission.SquadMissionChanger;
import atlantis.game.A;

import java.util.ArrayList;

public class ActWithSquadsCommander extends Commander {

    @Override
    protected void handle() {
        ArrayList<Squad> allSquads = AllSquads.allClone();

        // Act with every squad (which contains combat units)
        for (Squad squad : allSquads) {
            handleSquad(squad);
        }
    }

    private void handleSquad(Squad squad) {
        if (A.everyNthGameFrame(11)) {
            SquadMissionChanger.changeSquadMissionIfNeeded(squad);
        }

        (new ASquadCommander(squad)).invokeCommander();
    }
}
