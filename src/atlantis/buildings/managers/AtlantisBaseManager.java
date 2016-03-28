package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.enemy.AtlantisEnemyUnits;
import atlantis.units.AUnit;
import atlantis.workers.AtlantisWorkerCommander;


public class AtlantisBaseManager {

    public static void update(AUnit base) {

        // Train new workers if allowed
        if (AtlantisWorkerCommander.shouldTrainWorkers(true)) {
            if (hasSlotToProduceUnit(base)) {
                base.train(AtlantisConfig.WORKER);
            }
        }
    }
    
    // =========================================================

    private static boolean hasSlotToProduceUnit(AUnit base) {
        if (AtlantisGame.getSupplyFree() == 0) {
            return false;
        }
        
        if (AtlantisGame.playsAsZerg()) {
            return !base.getLarva().isEmpty();
        }
        else {
            return base.getTrainingQueue().isEmpty();
        }
    }

}
