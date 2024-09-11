package atlantis.combat;

import atlantis.architecture.Commander;
import atlantis.combat.missions.MissionCommander;
import atlantis.combat.squad.commanders.SquadsCommander;

public class CombatCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            MissionCommander.class,
            SquadsCommander.class,
        };
    }
}
