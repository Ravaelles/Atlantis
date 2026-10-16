package atlantis.combat;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.combat.missions.MissionCommander;
import atlantis.combat.squad.commanders.SquadsCommander;

public class CombatCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            MissionCommander::new,
            SquadsCommander::new,
        };
    }
}
