package atlantis.combat.squad.commanders;

import atlantis.architecture.Commander;
import atlantis.combat.squad.AllSquads;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.mission.SquadMissionChanger;
import atlantis.game.A;

import java.util.ArrayList;
import java.util.Objects;

public class ActWithSquadsCommander extends Commander {

    @Override
    protected void handle() {
        ArrayList<Squad> allSquads = AllSquads.allClone();

        // Act with every squad (which contains combat units)
        for (Squad squad : allSquads) {
//            if (!Objects.equals(squad.letter(), "A") && squad.size() > 0) {
//                System.out.println("ActWithSquadsCommander.handle() - squad: " + squad.name() + " / " + squad.size());
//            }
            handleSquad(squad);
        }
    }

    private void handleSquad(Squad squad) {
        if (A.everyNthGameFrame(17)) {
            SquadMissionChanger.changeSquadMissionIfNeeded(squad);
        }

        (new ASquadCommander(squad)).invokeCommander();
    }
}
