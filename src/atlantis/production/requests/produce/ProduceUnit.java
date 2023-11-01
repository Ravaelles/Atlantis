package atlantis.production.requests.produce;

import atlantis.game.AGame;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.units.AUnitType;

public class ProduceUnit {
    public static boolean produceUnit(AUnitType type) {
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
            return CurrentBuildOrder.get().produceUnit(type);
        }
//        else {
//            A.errPrintln("Can't afford " + type + " (" + type.getMineralPrice() + ", " + type.getGasPrice() + ")");
//        }

        return false;
    }
}
