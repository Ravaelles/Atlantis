package atlantis.production.requests.produce;

import atlantis.config.AtlantisConfig;
import atlantis.game.AGame;
import atlantis.production.dynamic.AutoTrainWorkersCommander;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProduceWorker {
    public static boolean produceWorker() {
        AUnit base = Select.ourOneNotTrainingUnits(AtlantisConfig.BASE);
        if (base == null) {
            return false;
        }

        if (We.zerg()) {
//            if (AGame.supplyUsed() <= 9) {
//                return true;
//            }
            if (AGame.supplyUsed() >= 10 && Count.larvas() <= 1) {
                return false;
            }
        }

        if (isSafeToProduceWorkerAt(base)) {
            return AutoTrainWorkersCommander.produceWorker(base);
        }

        return false;
    }

    protected static boolean isSafeToProduceWorkerAt(AUnit base) {
        return base.enemiesNear().havingWeapon().canAttack(base, 6).empty();
    }
}