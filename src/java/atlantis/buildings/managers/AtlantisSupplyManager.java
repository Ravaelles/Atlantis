package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;

public class AtlantisSupplyManager {

    private static int supplyTotal;
    private static int supplyFree;

    // =========================================================
    public static void update() {
        supplyTotal = AtlantisGame.getSupplyTotal();

        /**
         * Check if should use auto supply manager
         */
        if (AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS <= supplyTotal) {
            supplyFree = AtlantisGame.getSupplyFree();

            if (supplyTotal <= 20) {
                supply1to20();
            } else if (supplyTotal <= 60) {
                supply21to60();
            } else {
                supply61up();
            }
        }
    }

    // =========================================================
    private static void supply1to20() {
        if (!requestedConstructionOfSupply()) {
            if (supplyTotal <= 10 && supplyFree <= 1) {
                requestAdditionalSupply();
            } else if (supplyTotal <= 20 && supplyFree <= 3) {
                requestAdditionalSupply();
            }
        }
    }

    private static void supply21to60() {
        // && (!requestedConstructionOfSupply() || supplyFree <= 2)
        if (supplyFree <= 9 && !requestedConstructionOfSupply()) {
            System.out.println("SUPP = "
                    + AtlantisConstructingManager.countNotStartedConstructionsOfType(AtlantisConfig.SUPPLY));
            requestAdditionalSupply();
        }
    }

    private static void supply61up() {
        if (supplyFree <= 20 && supplyTotal < 200 && !requestedConstructionOfSupply()) {
            requestAdditionalSupply();
        }
    }

    // =========================================================
    private static void requestAdditionalSupply() {
        AtlantisConstructingManager.requestConstructionOf(AtlantisConfig.SUPPLY);
    }

    private static boolean requestedConstructionOfSupply() {
        return AtlantisConstructingManager.countNotStartedConstructionsOfType(AtlantisConfig.SUPPLY) > 0;
    }

}
