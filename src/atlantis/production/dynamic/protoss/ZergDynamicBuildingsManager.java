package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class ZergDynamicBuildingsManager {

    public static void update() {
//        buildingsIfNeeded();

        sunkens();
    }

    // =========================================================

    private static void sunkens() {
//        if (OurStrategicBuildings.antiLandBuildingsNeeded() > 0) {
//            if (OurStrategicBuildings.antiLandBuildingsNeeded() > Count.ofType(AUnitType.Zerg_Sunken_Colony)) {
        if (Count.ofType(AUnitType.Zerg_Creep_Colony) > 0 && A.hasMinerals(75)) {
            ZergBuildOrder.produceZergBuilding(AUnitType.Zerg_Sunken_Colony, null);
            return;
        }
//        }
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
