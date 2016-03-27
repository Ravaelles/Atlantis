package atlantis.buildings.managers;

import atlantis.AtlantisGame;
import atlantis.production.ProductionOrder;
import atlantis.wrappers.Select;
import java.util.ArrayList;
import bwapi.Unit;
import bwapi.UnitType;
//import bwapi.UnitType.UnitTypes;

public class AtlantisBarracksManager {

    public static void update(Unit barracks) {
        if (!AtlantisGame.playsAsZerg()) {
            if (shouldTrainUnit(barracks)) {
                if (hasEmptySlot(barracks)) {
                    buildUnit(barracks);
                }
            }
        }
    }

    // =========================================================
    private static boolean shouldTrainUnit(Unit barracks) {

        // Plays as TERRAN
        if (AtlantisGame.playsAsTerran()) {

            // Check MINERALS
            if (AtlantisGame.getMinerals() < 50) {
                return false;
            }

            // Check SUPPLY
            if (AtlantisGame.getSupplyFree() == 0) {
                return false;
            }
        }

        // =========================================================
        // Check PRODUCTION QUEUE
        ArrayList<ProductionOrder> unitsToProduce = AtlantisGame.getProductionStrategy().getThingsToProduceRightNow(
                true);

        // EMPTY PRODUCTION QUEUE - can build
        if (unitsToProduce.isEmpty()) {
            return true;
        } // Production queue not empty
        else {
            for (ProductionOrder order : unitsToProduce) {
                if (order.getUnitType().isOrganic()) {	//replaces  isInfantry()
                    return true;
                }
            }
        }

        return false;
    }

    // =========================================================
    private static void buildUnit(Unit barracks) {
        UnitType unitToBuild = defineUnitToBuild(barracks);
        if (unitToBuild != null) {
            barracks.train(unitToBuild);
        }
    }

    private static UnitType defineUnitToBuild(Unit barracks) {
        return UnitType.Terran_Marine;
    }

    private static boolean hasEmptySlot(Unit barracks) {
        if (AtlantisGame.playsAsZerg()) {
            return Select.ourLarva().count() > 0;
        } else {
            return barracks.getTrainingQueue().size() == 0;
        }
    }

}
