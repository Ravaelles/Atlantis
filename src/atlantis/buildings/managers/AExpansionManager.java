package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AGame;
import atlantis.constructing.AtlantisConstructionManager;
import static atlantis.constructing.AtlantisConstructionManager.requestConstructionOf;
import atlantis.constructing.ConstructionOrderStatus;
import atlantis.information.AtlantisMap;
import atlantis.production.ProductionOrder;
import atlantis.production.orders.ABuildOrdersManager;
import atlantis.units.Select;
import java.util.ArrayList;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AExpansionManager {

    public static void requestNewBaseIfNeeded() {
//        if (1 < 2) {
//            return;
//        }

        if (AGame.playsAsZerg() && !AGame.hasMinerals(1300)) {
            return;
        }
        
        int minMinerals = 100 + (AGame.playsAsZerg() ? 268 : 356);

        // It makes sense to think about expansion only if we have a lot of minerals.
        if (!AGame.hasMinerals(minMinerals)) {
            return;
        }

        // If there're still things to produce, don't auto-expand.
        ArrayList<ProductionOrder> nextOrders
                = ABuildOrdersManager.getBuildOrders().getProductionQueueNext(5);
        if (nextOrders.size() >= 3 && !AGame.hasMinerals(minMinerals + 50)) {
            return;
        }
        
        // === Force decent army before 3rd base =========================================
        
        int numberOfBases = Select.ourBases().count() 
                + AtlantisConstructionManager.countNotFinishedConstructionsOfType(AtlantisConfig.BASE);
        
        // Enforce too have a lot of tanks before expansion
        if (AGame.playsAsTerran() && numberOfBases >= 2) {
            if (Select.ourTanks().count() <= 8) {
                return;
            }
        }
        
        // === Check if we have almost as many bases as base locations; if so, exit ======
        
        if (numberOfBases >= AtlantisMap.getBaseLocations().size() - 2) {
            return;
        }

        // ===============================================================================
        int numberOfUnfinishedBases
                = AtlantisConstructionManager.countNotFinishedConstructionsOfType(AtlantisConfig.BASE);

        boolean haveEnoughMinerals = AGame.hasMinerals(minMinerals);
        boolean haveEnoughBases = numberOfBases >= 7
                && AGame.playsAsZerg() && Select.ourLarva().count() >= 2;
        boolean noBaseToConstruct = numberOfUnfinishedBases == 0;
        boolean allowExtraExpansion = AGame.hasMinerals(minMinerals + 200)
                && numberOfUnfinishedBases <= 1;

        // Check if it makes sense to request new base
        if (haveEnoughMinerals && !haveEnoughBases && (noBaseToConstruct || allowExtraExpansion)) {
            
            // ZERG case
            if (AGame.playsAsZerg() && AGame.hasMinerals(minMinerals)) {
                ProductionOrder fakeProductionOrder = new ProductionOrder(AtlantisConfig.BASE);
                fakeProductionOrder.setModifier(ProductionOrder.BASE_POSITION_MAIN);
                
                requestConstructionOf(
                        AtlantisConfig.BASE, 
                        fakeProductionOrder,
                        Select.secondBaseOrMainIfNoSecond().getPosition()
                );
            } 
            
            // TERRAN + PROTOSS
            else {
                requestConstructionOf(AtlantisConfig.BASE);
            }
        }
    }

}
