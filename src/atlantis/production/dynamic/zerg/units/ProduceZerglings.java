package atlantis.production.dynamic.zerg.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class ProduceZerglings {
    public static boolean zerglings() {
        if (!Have.a(AUnitType.Zerg_Spawning_Pool)) return false;

        int zerglings = Count.zerglings();
        int larvas = Count.larvas();

        if (zerglings >= 2 && larvas == 0) return false;

        if (Have.hydraliskDen()) {
            if (!A.hasMinerals(210) && zerglings >= 4) return false;
        }

        if (zerglings <= 5) return produceZergling();

        if (larvas <= (A.seconds() <= 200 ? 1 : 2)) return false;

        if (AGame.supplyFree() <= 1 || !AGame.canAffordWithReserved(150, 0)) return false;

        if (
            zerglings <= 40 && (AGame.canAffordWithReserved(50, 0))
        ) {
//            System.err.println(A.now() + " zergling enqueued");
            return produceZergling();
        }

        return false;

    }

    private static boolean produceZergling() {
        return AddToQueue.maxAtATime(AUnitType.Zerg_Zergling, 5);
    }
}
