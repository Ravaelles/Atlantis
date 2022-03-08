package atlantis.units.buildings;

import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.build.AddToQueue;
import atlantis.production.orders.build.BuildOrderSettings;
import atlantis.production.orders.build.CurrentBuildOrder;
import atlantis.production.orders.build.ZergBuildOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ASupplyManager {

    private static int supplyTotal;
    private static int supplyFree;

    // =========================================================
    
    public static void update() {
        supplyTotal = AGame.supplyTotal();

        if (supplyTotal >= 200) {
            return;
        }

        if (requestedConstructionsOfSupply() >= 1 && A.supplyTotal() <= 50) {
            return;
        }

        // Fix for UMS maps
        if (A.isUms() && AGame.supplyFree() <= 1) {
            requestAdditionalSupply();
            return;
        }

        // Should use auto supply manager
//        System.out.println(supplyTotal + " // " + CurrentBuildOrder.settingAutoSupplyManagerWhenSupplyExceeds());
        if (supplyTotal >= BuildOrderSettings.autoSupplyManagerWhenSupplyExceeds()) {
            supplyFree = AGame.supplyFree();

            int suppliesBeingBuilt = requestedConstructionsOfSupply();
            boolean noSuppliesBeingBuilt = suppliesBeingBuilt == 0;
            if (supplyTotal <= 11) {
                if (supplyFree <= 2 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 20) {
                if (supplyFree <= 3 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 43) {
                if (supplyFree <= 5 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 100) {
                if (supplyFree <= 10 && noSuppliesBeingBuilt) {
                    requestAdditionalSupply();
                }
            } else if (supplyTotal <= 200) {
                if (supplyFree <= 25 && suppliesBeingBuilt <= 1) {
                    requestAdditionalSupply();
                }
            }
        }
    }

    // =========================================================
    
    private static void requestAdditionalSupply() {
//        A.printStackTrace("Supply request: " + A.supplyUsed() + " // " + A.supplyTotal());

        // Zerg handles supply a bit differently
        if (AGame.isPlayingAsZerg()) {
            ((ZergBuildOrder) CurrentBuildOrder.get()).produceZergUnit(AUnitType.Zerg_Overlord);
        } 

        // Terran + Protoss
        else {
            AddToQueue.withHighPriority(AtlantisConfig.SUPPLY);
        }
    }

    private static boolean requestedConstructionOfSupply() {
        return ConstructionRequests.countNotStartedOfType(AtlantisConfig.SUPPLY) > 0;
    }

    private static int requestedConstructionsOfSupply() {
        if (We.zerg()) {
//            return Count.ourOfTypeIncludingUnfinished(AUnitType.Zerg_Overlord);
            return Count.inProductionOrInQueue(AUnitType.Zerg_Overlord);
        }

        return Count.inProductionOrInQueue(AtlantisConfig.SUPPLY);

//        return ConstructionRequests.countNotFinishedConstructionsOfType(AtlantisConfig.SUPPLY);
//
//        // =========================================================
//        // Terran + Protoss
//        else {
//            return Select.ourUnfinished().ofType(AUnitType.UnitTypes.Zerg_Overlord).count();
//        }
    }

}
