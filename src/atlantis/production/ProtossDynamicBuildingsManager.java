package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnitType;

public class ProtossDynamicBuildingsManager extends ADynamicBuildingsManager {

    public static void update() {
        if (AGame.notNthGameFrame(7) || noSupply(25)) {
            return;
        }

        gateways();
        forge();
        stargate();
        arbiterTribunal();
    }

    // =========================================================

    private static void gateways() {
        buildIfAllBusyButCanAfford(AUnitType.Protoss_Gateway);
    }

    private static void forge() {
        buildToHaveOne(30, AUnitType.Protoss_Forge);
    }

    private static void stargate() {
        buildToHaveOne(70, AUnitType.Protoss_Stargate);
    }

    private static void arbiterTribunal() {
        buildToHaveOne(90, AUnitType.Protoss_Arbiter_Tribunal);
    }
}
