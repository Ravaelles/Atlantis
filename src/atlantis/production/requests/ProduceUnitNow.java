package atlantis.production.requests;

import atlantis.config.AtlantisConfig;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.AutoTrainWorkersCommander;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.production.orders.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ProduceUnitNow {
    public static void produceBuilding(AUnitType type, ProductionOrder order) {
        assert type.isBuilding();

        if (type.isZerg()) {
            ZergBuildOrder.produceZergBuilding(type, order);
            return;
        }

        if (type.isAddon()) {
            produceAddon(type);
        } else {
            ConstructionRequests.requestConstructionOf(order);
        }
    }

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
            return produceWorker();
        }

        // =========================================================
        // Non-worker

        else if (AGame.canAfford(type.getMineralPrice(), type.getGasPrice())) {
            return CurrentBuildOrder.get().produceUnit(type);
        }

        return false;
    }

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

    public static void produceAddon(AUnitType addon) {
        for (AUnit building : Select.ourOfType(addon.whatBuildsIt()).free().list()) {
            building.buildAddon(addon);
            return;
        }
    }

    protected static boolean isSafeToProduceWorkerAt(AUnit base) {
        return base.enemiesNear().excludeTypes(
            AUnitType.Zerg_Overlord, AUnitType.Protoss_Observer
        ).inRadius(10, base).atMost(1);
    }
}
