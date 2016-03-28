package atlantis.buildings.managers;

import atlantis.AtlantisGame;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import java.util.ArrayList;

import bwapi.UnitType;
//import bwapi.AUnitType.UnitTypes;

public class AtlantisBarracksManager {

    public static void update(AUnit barracks) {
        if (!AtlantisGame.playsAsZerg()) {
            if (shouldTrainUnit(barracks)) {
                if (hasEmptySlot(barracks)) {
                    buildUnit(barracks);
                }
            }
        }
    }

    // =========================================================
    
    private static boolean shouldTrainUnit(AUnit barracks) {

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
        ArrayList<ProductionOrder> unitsToProduce = AtlantisGame.getBuildOrders().getThingsToProduceRightNow(
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
    
    private static void buildUnit(AUnit barracks) {
        AUnitType unitToBuild = defineUnitToBuild(barracks);
        if (unitToBuild != null) {
            barracks.train(unitToBuild);
        }
    }

    private static AUnitType defineUnitToBuild(AUnit barracks) {
        return AUnitType.Terran_Marine;
    }

    private static boolean hasEmptySlot(AUnit barracks) {
        if (AtlantisGame.playsAsZerg()) {
            return Select.ourLarva().count() > 0;
        } else {
            return barracks.getTrainingQueue().size() == 0;
        }
    }

}
