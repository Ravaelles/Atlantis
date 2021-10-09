package atlantis.production;

import atlantis.AGame;
import atlantis.constructing.AConstructionManager;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ProtossDynamicBuildingsManager {

    public static void update() {
        if (AGame.getSupplyTotal() < 25) {
            return;
        }

        gatewaysIfNeeded();
    }

    // =========================================================

    private static void gatewaysIfNeeded() {
        if (AGame.canAfford(358, 0)) {
            if (Select.ourOfType(AUnitType.Protoss_Gateway).areAllBusy()) {
                AConstructionManager.requestConstructionOf(AUnitType.Protoss_Gateway);
            }
        }
    }
}
