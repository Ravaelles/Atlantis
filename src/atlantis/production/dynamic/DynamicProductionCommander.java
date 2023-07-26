package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.AGame;

public class DynamicProductionCommander extends Commander {
    @Override
    public boolean applies() {
        return !AGame.isUms();
    }

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[] {
            DynamicProductionOfUnitsCommander.class,
            DynamicBuildingsCommander.class,
        };
    }
    
}
