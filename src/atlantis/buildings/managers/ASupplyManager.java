package atlantis.buildings.managers;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.orders.AddToQueue;
import atlantis.production.orders.ProductionQueue;
import atlantis.production.orders.ZergBuildOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.Us;

public class ASupplyManager {

    private static int supplyTotal;
    private static int supplyFree;

    // =========================================================
    
    public static void update() {
        supplyTotal = AGame.supplyTotal();

        // Should use auto supply manager
        System.out.println(supplyTotal + " // " + AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS);
        if (supplyTotal >= AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS) {
            supplyFree = AGame.supplyFree();

            int suppliesBeingBuilt = requestedConstructionsOfSupply();
            System.out.println("suppliesBeingBuilt = " + suppliesBeingBuilt);
            boolean noSuppliesBeingBuilt = suppliesBeingBuilt == 0;
            if (supplyTotal <= 11) {
                if (supplyFree <= 2 && noSuppliesBeingBuilt) {
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

        // Zerg handles supply a bit differently
        if (AGame.isPlayingAsZerg()) {
            ((ZergBuildOrder) ProductionQueue.get()).produceZergUnit(AUnitType.Zerg_Overlord);
        } 

        // Terran + Protoss
        else {
            AddToQueue.addWithHighPriority(AtlantisConfig.SUPPLY);
        }
    }

    private static boolean requestedConstructionOfSupply() {
        return AConstructionRequests.countNotStartedConstructionsOfType(AtlantisConfig.SUPPLY) > 0;
    }

    private static int requestedConstructionsOfSupply() {
        if (Us.isZerg()) {
            return Count.ourOfTypeIncludingUnfinished(AUnitType.Zerg_Overlord);
        }

        return AConstructionRequests.countNotFinishedConstructionsOfType(AtlantisConfig.SUPPLY);

//
//        // =========================================================
//        // Terran + Protoss
//        else {
//            return Select.ourUnfinished().ofType(AUnitType.UnitTypes.Zerg_Overlord).count();
//        }
    }

}
