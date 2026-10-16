package atlantis.combat.missions;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;

public class MissionCommander extends Commander {

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            GlobalMissionCommander::new,
        };
    }

}
