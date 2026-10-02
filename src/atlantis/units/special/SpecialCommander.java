package atlantis.units.special;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.information.decisions.ForceEarlyGGOnlyLocally;
import atlantis.information.decisions.ForceExitLocallyAfterRealSeconds;
import atlantis.information.decisions.GG;
import atlantis.information.decisions.GGForEnemy;
import atlantis.terran.chokeblockers.ChokeBlockersCommander;
import atlantis.terran.repair.TerranRepairsCommander;
import atlantis.units.workers.defence.proxy.TrackEnemyEarlyScoutCommander;

public class SpecialCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
            TrackEnemyEarlyScoutCommander::new,
            TerranRepairsCommander::new,
            ChokeBlockersCommander::new,
            SpecialUnitsCommander::new,

            GG::new,
            GGForEnemy::new,
            ForceEarlyGGOnlyLocally::new,
            ForceExitLocallyAfterRealSeconds::new,
        };
    }
}
