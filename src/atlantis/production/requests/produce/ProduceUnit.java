package atlantis.production.requests.produce;

import atlantis.game.AGame;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.units.AUnitType;

public class ProduceUnit {
    public static boolean produceUnit(AUnitType type) {
        assert !type.isBuilding();

        // Supply: OVERLORD / PYLON / DEPOT
//        if (AGame.supplyFree() == 0 && !unitType.isSupplyUnit()) {
//            // Supply production is handled by AtlantisSupplyManager
//            return false;
//        }

        // =========================================================
        // Worker

//        System.out.println(type + " (" + A.supplyUsed() + ")");
        if (type.isWorker()) {
//            System.out.println(A.now() + " worker");
            return ProduceWorker.produceWorker();
        }

        // =========================================================
        // Non-worker

        else if (AGame.canAfford(type.getMineralPrice(), type.getGasPrice())) {
            return CurrentBuildOrder.get().produceUnit(type);
        }

        return false;
    }
}