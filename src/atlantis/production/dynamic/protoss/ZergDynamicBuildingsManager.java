package atlantis.production.dynamic.protoss;

import atlantis.production.orders.ZergBuildOrder;
import atlantis.production.requests.AAntiLandBuildingRequests;
import atlantis.strategy.decisions.OurStrategicBuildings;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ZergDynamicBuildingsManager {

    public static void update() {
//        buildingsIfNeeded();

        sunkens();
    }

    // =========================================================

    private static void sunkens() {
        if (OurStrategicBuildings.antiLandBuildingsNeeded() > 0) {
            if (OurStrategicBuildings.antiLandBuildingsNeeded() > Count.ofType(AUnitType.Zerg_Sunken_Colony)) {
                ZergBuildOrder.produceZergBuilding(AUnitType.Zerg_Sunken_Colony, null);
                return;
            }
        }
    }

//    private static void buildingsIfNeeded() {
//        for (AUnit gateway : Select.ourOfType(AUnitType.Zerg_).listUnits()) {
//            if (!gateway.isTrainingAnyUnit()) {
//                if (AGame.canAfford(200, 0)) {
//                    AConstructionManager.requestConstructionOf(AUnitType.Zerg_);
//                }
//            }
//        }
//    }

}
