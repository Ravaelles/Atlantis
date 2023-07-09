package atlantis.combat.squad;

import atlantis.architecture.Commander;
import atlantis.combat.squad.mission.SquadMissionChanger;
import atlantis.game.A;

public class SquadHandlerCommander extends Commander {

    @Override
    public void handle() {
        // Act with every combat unit
        for (Squad squad : AllSquads.all()) {
            handleSquad(squad);
        }
    }

    private static void handleSquad(Squad squad) {
        if (A.everyNthGameFrame(11)) {
            SquadMissionChanger.changeSquadMissionIfNeeded(squad);
        }

        (new ASquadManager(squad)).update();
    }
}
