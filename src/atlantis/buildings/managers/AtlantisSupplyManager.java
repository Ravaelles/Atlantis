package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructionManager;
import atlantis.production.orders.ZergBuildOrders;
import atlantis.units.AUnitType;

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

            int suppliesBeingBuilt = requestedConstructionOfSupplyNumber();
            boolean noSuppliesBeingBuilt = suppliesBeingBuilt == 0;
            if (supplyTotal <= 10) {
                if (supplyFree <= 2 && noSuppliesBeingBuilt) {
                    AtlantisGame.sendMessage("Request supply");
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 20) {
                if (supplyFree <= 4 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 40) {
                if (supplyFree <= 7 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 100) {
                if (supplyFree <= 10 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 200) {
                if (supplyFree <= 14 && suppliesBeingBuilt <= 1) {
                    requestAdditionalSupply();
                }
            }
        }
    }

    // =========================================================
    
    private static void requestAdditionalSupply() {

        // Zerg
        if (AtlantisGame.playsAsZerg()) {
            ((ZergBuildOrders) AtlantisGame.getBuildOrders()).produceZergUnit(AUnitType.Zerg_Overlord);
        } // Terran + Protoss
        else {
            AtlantisConstructionManager.requestConstructionOf(AtlantisConfig.SUPPLY);
        }
    }

    private static boolean requestedConstructionOfSupply() {
        return AtlantisConstructionManager.countNotStartedConstructionsOfType(AtlantisConfig.SUPPLY) > 0;
    }

    private static int requestedConstructionOfSupplyNumber() {
        return AtlantisConstructionManager.countNotFinishedConstructionsOfType(AtlantisConfig.SUPPLY);
        
        // Zerg
//        if (AtlantisGame.playsAsZerg()) {
//            return AtlantisConstructingManager.countNotFinishedConstructionsOfType(AtlantisConfig.SUPPLY);
//        }
//        
//        // =========================================================
//        // Terran + Protoss
//        else {
//            return Select.ourUnfinished().ofType(AUnitType.UnitTypes.Zerg_Overlord).count();
//        }
    }

}
