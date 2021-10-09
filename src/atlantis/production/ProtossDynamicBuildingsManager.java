package atlantis.production;

import atlantis.AGame;
import atlantis.constructing.AConstructionRequests;
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
                buildIfPossible(AUnitType.Protoss_Gateway, true, 150, 0);
            }
        }
    }

    protected static void buildIfPossible(AUnitType unitType, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!AGame.canAfford(hasMinerals, hasGas)) {
            return;
        }

        if (onlyOneAtTime && AConstructionRequests.hasRequestedConstructionOf(unitType)) {
            return;
        }

        AConstructionRequests.requestConstructionOf(AUnitType.Protoss_Gateway);
    }
}
