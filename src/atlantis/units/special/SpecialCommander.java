package atlantis.units.special;

import atlantis.architecture.Commander;
import atlantis.information.decisions.ForceEarlyGGOnlyLocally;
import atlantis.information.decisions.ForceExitLocallyAfterRealSeconds;
import atlantis.information.decisions.GG;
import atlantis.information.decisions.GGForEnemy;
import atlantis.terran.chokeblockers.ChokeBlockersCommander;
import atlantis.terran.repair.TerranRepairsCommander;
import atlantis.units.workers.defence.proxy.TrackEnemyEarlyScoutCommander;

public class SpecialCommander extends Commander {
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            TrackEnemyEarlyScoutCommander.class,
            TerranRepairsCommander.class,
            ChokeBlockersCommander.class,
            SpecialUnitsCommander.class,

            GG.class,
            GGForEnemy.class,
            ForceEarlyGGOnlyLocally.class,
            ForceExitLocallyAfterRealSeconds.class,
        };
    }
}
