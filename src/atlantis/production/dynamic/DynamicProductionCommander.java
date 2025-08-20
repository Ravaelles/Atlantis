package atlantis.production.dynamic;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.units.select.Have;

public class DynamicProductionCommander extends Commander {
    @Override
    public boolean applies() {
        return !AGame.isUms() || Have.anyBuilding();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            AutoProduceWorkersCommander.class,
            DynamicUnitAndTechProducerCommander.class,
            DynamicBuildingsCommander.class,
        };
    }

}
