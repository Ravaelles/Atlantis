package atlantis.production.requests.produce;

import atlantis.game.AGame;
import atlantis.production.dynamic.AutoTrainWorkersCommander;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.combat.micro.terran.lifted.RebaseToNewMineralPatches.isBaseMinedOut;

public class ProduceWorker {
    public static boolean produceWorker() {
        AUnit base = baseToProduceWorker();

        if (base == null) return false;

        if (We.zerg()) {
//            if (AGame.supplyUsed() <= 9) {
//                return true;
//            }
            if (AGame.supplyUsed() >= 10 && Count.larvas() <= 1) {
                return false;
            }
        }

        return AutoTrainWorkersCommander.produceWorker(base);
    }

    private static AUnit baseToProduceWorker() {
        for (AUnit base : Select.ourBases().free().notLifted().list()) {
            if (!isSafeToProduceWorkerAt(base)) continue;
            if (isBaseMinedOut(base)) continue;

            return base;
        }

        return null;
    }

    protected static boolean isSafeToProduceWorkerAt(AUnit base) {
        return base.enemiesNear().groundUnits().canAttack(base, 6).atMost(1);
    }
}