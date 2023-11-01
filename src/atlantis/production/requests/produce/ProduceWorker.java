package atlantis.production.requests.produce;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.dynamic.AutoTrainWorkersCommander;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

import static atlantis.combat.micro.terran.lifted.RebaseToNewMineralPatches.isBaseMinedOut;

public class ProduceWorker {
    public static boolean produceWorker() {
        if (!hasEnoughMineralsToProduceWorker()) return false;

        if (We.zerg()) {
            if (AGame.supplyUsed() >= 10 && Count.larvas() <= 1) return false;
        }

        AUnit base = baseToProduceWorker();

        if (base == null) return false;

        return AutoTrainWorkersCommander.produceWorker(base);
    }

    private static boolean hasEnoughMineralsToProduceWorker() {
        return A.minerals() >= 200 || (ReservedResources.minerals() - A.minerals()) >= 50;
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