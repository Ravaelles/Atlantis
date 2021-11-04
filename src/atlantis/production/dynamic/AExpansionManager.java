package atlantis.production.dynamic;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.map.BaseLocations;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.orders.AddToQueue;
import atlantis.production.orders.ProductionQueue;
import atlantis.units.select.Select;

public class AExpansionManager {

    public static void requestNewBaseIfNeeded() {
        boolean hasPlentyOfMinerals = AGame.hasMinerals(600);
        int minMinerals = 100 + (AGame.isPlayingAsZerg() ? 268 : 356);

        // It makes sense to think about expansion only if we have a lot of minerals.
        if (!AGame.hasMinerals(minMinerals)) {
            return;
        }

        // If we have lenty of minerals, then every new base is a hazard
        if (AGame.hasMinerals(1200)) {
            return;
        }

        int inConstruction = ProductionQueue.countInQueue(AtlantisConfig.BASE, 8);
        if (inConstruction >= 1) {
            return;
        }

        // If there're still things to produce, don't auto-expand.
//        ArrayList<ProductionOrder> nextOrders = ProductionQueue.getProductionQueueNext(5);
//        if (nextOrders.size() >= 3 && !AGame.hasMinerals(minMinerals + 50)) {
//            return;
//        }

        // === Force decent army before 3rd base =========================================

        // Enforce too have a lot of tanks before expansion
        int numberOfBases = Select.ourBases().count() + inConstruction;
        if (!hasPlentyOfMinerals && AGame.isPlayingAsTerran() && numberOfBases >= 2) {
            if (Select.ourTanks().count() <= 8) {
                return;
            }
        }
        
        // === Check if we have almost as many bases as base locations; if so, exit ======
        
        if (numberOfBases >= BaseLocations.baseLocations().size() - 2) {
            return;
        }

        // ===============================================================================

        int numberOfUnfinishedBases = AConstructionRequests.countNotFinishedConstructionsOfType(AtlantisConfig.BASE);

        boolean haveEnoughMinerals = AGame.hasMinerals(minMinerals);
        boolean haveEnoughBases = numberOfBases >= 7
                && AGame.isPlayingAsZerg() && Select.ourLarva().count() >= 2;
        boolean noBaseToConstruct = numberOfUnfinishedBases == 0;
        boolean allowExtraExpansion = AGame.hasMinerals(minMinerals + 200)
                && numberOfUnfinishedBases <= 1;

        // Check if it makes sense to request new base
        if (haveEnoughMinerals && !haveEnoughBases && (noBaseToConstruct || allowExtraExpansion)) {

            // ZERG case
            if (AGame.isPlayingAsZerg()) {
                AddToQueue.addWithHighPriority(AtlantisConfig.BASE, Select.naturalBaseOrMain().position());
            }
            
            // TERRAN + PROTOSS
            else {
                AddToQueue.addWithHighPriority(AtlantisConfig.BASE);
            }
        }
    }

}
