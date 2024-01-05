package atlantis.production.dynamic.zerg.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ForcedDirectProductionOrder;
import atlantis.production.orders.zerg.ProduceZergUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.*;

public class ProduceZerglings {
    public static boolean zerglings() {
        if (!Have.a(AUnitType.Zerg_Spawning_Pool)) return false;

        int zerglings = Count.zerglings();
        int larvas = Count.larvas();

        if (larvas <= 1 && zerglings >= 2) return false;

        if (A.hasGas(150) && Have.hydraliskDen()) return false;

        if (larvas >= 3 && A.supplyUsed() <= 180 && A.hasMinerals(600)) return produceZergling();

        if (Have.hydraliskDen()) {
            if (!A.hasMinerals(210) && zerglings >= 4) return false;
        }

        if (zerglings <= 5 && (A.hasMinerals(500) || !A.hasGas(300))) return produceZergling();

        if (larvas >= 2 && A.supplyUsed() <= 180 && A.hasMinerals(400)) return produceZergling();

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
//        return AddToQueue.maxAtATime(AUnitType.Zerg_Zergling, 5) != null;
        return ProduceZergUnit.produceZergUnit(
            Zerg_Zergling, ForcedDirectProductionOrder.create(Zerg_Zergling)
        );
    }
}
