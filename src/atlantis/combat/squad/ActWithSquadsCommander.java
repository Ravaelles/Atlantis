package atlantis.combat.squad;

import atlantis.architecture.Commander;
import atlantis.combat.squad.mission.SquadMissionChanger;
import atlantis.game.A;

import java.util.ArrayList;
import java.util.Iterator;

public class ActWithSquadsCommander extends Commander {

    @Override
    public void handle() {
        // Act with every combat unit
        Iterator<Squad> squadsIterator = AllSquads.all().iterator();

        while (squadsIterator.hasNext()) {
            Squad squad = squadsIterator.next();
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
