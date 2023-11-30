package atlantis.production.requests.produce;

import atlantis.game.AGame;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public class ProduceUnit {
    public static boolean produceUnit(AUnitType type, ProductionOrder order) {
        assert !type.isABuilding();

        // Supply: OVERLORD / PYLON / DEPOT
//        if (AGame.supplyFree() == 0 && !unitType.isSupplyUnit()) {
//            // Supply production is handled by AtlantisSupplyManager
//            return false;
//        }

        // =========================================================
        // Worker

        if (type.isWorker()) {
            return ProduceWorker.produceWorker();
        }

        // =========================================================
        // Non-worker

        else if (AGame.canAfford(type.getMineralPrice(), type.getGasPrice())) {
            return CurrentBuildOrder.get().produceUnit(type, order);
        }
//        else {
//            A.errPrintln("Can't afford " + type + " (" + type.getMineralPrice() + ", " + type.getGasPrice() + ")");
//        }

        return false;
    }
}
