package atlantis.buildings.managers;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.constructing.AtlantisConstructingManager;
import atlantis.units.Select;
import atlantis.production.orders.AtlantisBuildOrders;
import java.util.ArrayList;
import static atlantis.constructing.AtlantisConstructingManager.requestConstructionOf;
import atlantis.production.ProductionOrder;
import static atlantis.constructing.AtlantisConstructingManager.requestConstructionOf;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisExpansionManager {

    public static void requestNewBaseIfNeeded() {
        int raceBonus = AtlantisGame.playsAsZerg() ? 100 : 0;
        
        // It makes sense to think about expansion only if we have a lot of minerals.
        if (!AtlantisGame.hasMinerals(376 - raceBonus)) {
            return;
        }
        
        // If there're still things to produce, don't auto-expand.
        ArrayList<ProductionOrder> nextOrders = 
                AtlantisBuildOrders.getBuildOrders().getProductionQueueNext(5);
        if (nextOrders.size() >= 3 && !AtlantisGame.hasMinerals(400 - raceBonus)) {
            return;
        }
        
        // =========================================================
        
        boolean haveEnoughMinerals = AtlantisGame.hasMinerals(376 - raceBonus);
        boolean haveEnoughBases = Select.ourBases().count() >= 7 
                && AtlantisGame.playsAsZerg() && Select.ourLarva().count() >= 2;
        boolean areWeAlreadyExpanding = 
                AtlantisConstructingManager.countNotFinishedConstructionsOfType(AtlantisConfig.BASE) == 0;
        boolean allowExtraExpansion = AtlantisGame.hasMinerals(550 - raceBonus);
        if (haveEnoughMinerals && !haveEnoughBases && (!areWeAlreadyExpanding || allowExtraExpansion)) {
            if (AtlantisConstructingManager.countNotFinishedConstructionsOfType(AtlantisConfig.BASE) <= 1) {
                if (!AtlantisGame.hasMinerals(550 - raceBonus)) {
                    requestConstructionOf(AtlantisConfig.BASE);
                }
                else {
                    requestConstructionOf(AtlantisConfig.BASE, Select.mainBase().getPosition());
                }
            }
        }
    }
    
}
