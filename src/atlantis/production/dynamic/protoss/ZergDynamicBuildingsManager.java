package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.production.dynamic.ADynamicBuildingsManager;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.*;

public class ZergDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
//        buildingsIfNeeded();

//        sunkens();
        hydraDen();
    }

    // =========================================================

    private static void hydraDen() {
        if (Have.a(Zerg_Hydralisk_Den) || Have.unfinishedOrPlanned(Zerg_Hydralisk_Den)) {
            return;
        }

        if (A.supplyUsed() <= 13 || Count.bases() <= 1) {
            return;
        }

//        if (Count.withPlanned(Zerg_Hydralisk_Den) == 0) {
        buildToHaveOne(Zerg_Hydralisk_Den);
//        }
    }

}
