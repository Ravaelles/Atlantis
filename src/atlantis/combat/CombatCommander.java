package atlantis.combat;

import atlantis.architecture.Commander;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.MissionCommander;
import atlantis.combat.missions.Missions;
import atlantis.combat.squad.ASquadManager;
import atlantis.combat.squad.Squad;
import atlantis.combat.squad.SquadCommander;
import atlantis.combat.squad.SquadTransfers;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.combat.squad.beta.Beta;
import atlantis.combat.squad.delta.Delta;
import atlantis.game.A;

import java.util.Iterator;

public class CombatCommander extends Commander {

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            MissionCommander.class,
            SquadCommander.class,
        };
    }

//    @Override
//    public void handle() {
//        updateGlobalMission();
//        updateSquads();
//    }

}
