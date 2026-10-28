package atlantis.production.dynamic.terran.turrets;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.production.dynamic.terran.turrets.offensive.TurretsToContainEnemy;

public class OffensiveTurretsCommander extends Commander {
//    @Override
//    public boolean applies() {
//        return false;
//    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{
//            TurretNeededHereCommander.class
            TurretsToContainEnemy::new
        };
    }
}
