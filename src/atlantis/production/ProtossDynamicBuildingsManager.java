package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ProtossDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        if (AGame.getSupplyTotal() < 25) {
            return;
        }

        gateways();
        forge();
        stargate();
        arbiter();
    }

    // =========================================================

    private static void gateways() {
        buildIfAllBusyButCanAfford(AUnitType.Protoss_Gateway);
    }

    private static void forge() {
        buildIfCanAfford(AUnitType.Protoss_Forge);
    }

    private static void stargate() {
        buildToHaveOne(AUnitType.Protoss_Stargate);
    }

    private static void arbiter() {
        buildToHaveOne(AUnitType.Protoss_Arbiter);
    }
}
