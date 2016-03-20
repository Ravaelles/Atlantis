package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.information.AtlantisUnitInformationManager;
import atlantis.workers.AtlantisWorkerCommander;
import jnibwapi.Unit;

public class AtlantisBaseManager {

    public static void update(Unit base) {

        // Train new workers if allowed
        if (AtlantisWorkerCommander.shouldTrainWorkers(true)) {
            base.train(AtlantisConfig.WORKER);
        }
    }

}
