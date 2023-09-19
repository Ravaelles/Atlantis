package atlantis.production.dynamic.zerg;

import atlantis.game.A;
import atlantis.production.dynamic.DynamicBuildingsCommander;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Zerg_Creep_Colony;
import static atlantis.units.AUnitType.Zerg_Hydralisk_Den;

public class ZergDynamicBuildingsCommander extends DynamicCommanderHelpers {
    @Override
    protected void handle() {
        hydraDen();
    }

    // =========================================================

    private static void hydraDen() {
        if (Have.a(Zerg_Hydralisk_Den) || Have.unfinishedOrPlanned(Zerg_Hydralisk_Den)) {
            return;
        }

        if (!A.hasGas(50)) {
            return;
        }

        if (A.supplyUsed() <= 13 || Count.bases() <= 1) {
            return;
        }

        if (!A.hasMinerals(120) && Count.existing(Zerg_Creep_Colony) > 0) {
            return;
        }

        DynamicCommanderHelpers.buildToHaveOne(Zerg_Hydralisk_Den);
    }

}
