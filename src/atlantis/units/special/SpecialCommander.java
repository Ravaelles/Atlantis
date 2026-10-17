package atlantis.units.special;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.information.decisions.*;
import atlantis.decisions.QuitWhenNoUnitsLeft;
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
            ForceExitLocallyAfterSomeTime::new,
            ForceExitWhenInvalidState::new,
            // ForceExitLocallyInOpenBW is NOT here: registering it from core added a new
            // core -> information violation, which CONVENTIONS §5 forbids (never grow the
            // frozen baseline). It is registered from ProductionCommander instead, in a
            // package allowed to depend on information - same move as
            // ForceExitWhenOpeningProven.
            QuitWhenNoUnitsLeft::new,
        };
    }
}
