package atlantis.production.dynamic.reinforce.terran.turrets;

import atlantis.architecture.Commander;
import atlantis.production.dynamic.reinforce.terran.turrets.offensive.TurretsToContainEnemy;

public class OffensiveTurretsCommander extends Commander {
//    @Override
//    public boolean applies() {
//        return false;
//    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
//            TurretNeededHereCommander.class
            TurretsToContainEnemy.class
        };
    }
}
