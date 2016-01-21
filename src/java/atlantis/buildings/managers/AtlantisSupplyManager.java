package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.production.strategies.ZergProductionStrategy;
import atlantis.wrappers.SelectUnits;
import jnibwapi.types.UnitType;

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

//            if (supplyTotal <= 20) {
//                supply1to20();
//            } else if (supplyTotal <= 60) {
//                supply21to60();
//            } else {
//                supply61up();
//            }
            int suppliesBeingBuilt = requestedConstructionOfSupplyNumber();
            boolean noSuppliesBeingBuilt = suppliesBeingBuilt == 0;
//            System.out.println(supplyFree + " / supply in prod: " + suppliesBeingBuilt);
            if (supplyTotal <= 10) {
                if (supplyFree <= 2 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 20) {
                if (supplyFree <= 4 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 40) {
                if (supplyFree <= 8 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 200) {
                if (supplyFree <= 14 && suppliesBeingBuilt <= 1) {
                    requestAdditionalSupply();
                }
            }

//                if (supplyTotal <= 10 && supplyFree <= 1) {
//                    requestAdditionalSupply();
//                } else if (supplyTotal <= 20 && supplyFree <= 4) {
//                    requestAdditionalSupply();
//                } else if (supplyTotal <= 50 && supplyFree <= 10) {
//                    requestAdditionalSupply();
//                } else if (supplyFree <= 20 && supplyTotal < 200) {
//                    requestAdditionalSupply();
//                }
        }
    }

    // =========================================================
    private static void requestAdditionalSupply() {

        // Zerg
        if (AtlantisConfig.SUPPLY.equals(UnitType.UnitTypes.Zerg_Overlord)) {
            ((ZergProductionStrategy) AtlantisGame.getProductionStrategy()).produceZergUnit(UnitType.UnitTypes.Zerg_Overlord);
        } // Terran + Protoss
        else {
            AtlantisConstructingManager.requestConstructionOf(AtlantisConfig.SUPPLY);
        }
    }

    private static boolean requestedConstructionOfSupply() {
        return AtlantisConstructingManager.countNotStartedConstructionsOfType(AtlantisConfig.SUPPLY) > 0;
    }

    private static int requestedConstructionOfSupplyNumber() {
        return AtlantisConstructingManager.countNotFinishedConstructionsOfType(AtlantisConfig.SUPPLY);
        
        // Zerg
//        if (AtlantisGame.playsAsZerg()) {
//            return AtlantisConstructingManager.countNotFinishedConstructionsOfType(AtlantisConfig.SUPPLY);
//        }
//        
//        // =========================================================
//        // Terran + Protoss
//        else {
//            return SelectUnits.ourUnfinished().ofType(UnitType.UnitTypes.Zerg_Overlord).count();
//        }
    }

}
