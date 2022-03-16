package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.information.decisions.Decisions;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.*;

public class ZergDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
//        buildingsIfNeeded();

        sunkens();
        hydraDen();
    }

    // =========================================================

    private static void hydraDen() {
        if (Have.a(Zerg_Hydralisk_Den)) {
            return;
        }

        if (Count.WithPlanned(Zerg_Hydralisk_Den) == 0) {
            buildToHaveOne(Zerg_Hydralisk_Den);
        }
    }

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
