package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.units.Select;
import atlantis.production.strategies.AtlantisProductionStrategy;
import java.util.ArrayList;
import static atlantis.constructing.AtlantisConstructingManager.requestConstructionOf;
import atlantis.production.ProductionOrder;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisExpansionManager {

    public static void requestNewBaseIfNeeded() {
        
        // It makes sense to think about expansion only if we have a lot of minerals.
        if (!AtlantisGame.hasMinerals(400)) {
            return;
        }
        
        // If there're still things to produce, don't auto-expand.
        ArrayList<ProductionOrder> nextOrders = 
                AtlantisProductionStrategy.getProductionStrategy().getProductionQueueNext(5);
        if (nextOrders.size() >= 3 && !AtlantisGame.hasMinerals(500)) {
            return;
        }
        
        // =========================================================
        
        boolean haveEnoughMinerals = AtlantisGame.hasMinerals(490)
                || (AtlantisGame.playsAsZerg() && AtlantisGame.hasMinerals(392));
        boolean haveEnoughBases = Select.ourBases().count() >= 7 
                && (!AtlantisGame.playsAsZerg() && Select.ourLarva().count() >= 1);
        boolean areWeAlreadyExpanding = 
                AtlantisConstructingManager.countNotFinishedConstructionsOfType(AtlantisConfig.BASE) == 0;
        boolean allowExtraExpansion = AtlantisGame.hasMinerals(750);
        if (haveEnoughMinerals && !haveEnoughBases && (!areWeAlreadyExpanding || allowExtraExpansion)) {
            if (AtlantisConstructingManager.countNotFinishedConstructionsOfType(AtlantisConfig.BASE) <= 1) {
                if (!AtlantisGame.hasMinerals(750)) {
                    requestConstructionOf(AtlantisConfig.BASE);
                }
                else {
                    requestConstructionOf(AtlantisConfig.BASE, Select.mainBase().getPosition());
                }
            }
        }
    }
    
}
