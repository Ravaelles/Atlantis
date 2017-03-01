package atlantis.buildings.managers;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class ABarracksManager {

    public static void update(AUnit barracks) {
//        if (!AGame.playsAsZerg()) {
//            if (shouldTrainUnit(barracks)) {
//                if (hasEmptySlot(barracks)) {
//                    buildUnit(barracks);
//                }
//            }
//        }
    }

    // =========================================================
    
//    private static boolean shouldTrainUnit(AUnit barracks) {
//
//        // Plays as TERRAN
//        if (AGame.playsAsTerran()) {
//
//            // Check MINERALS
//            if (AGame.getMinerals() < 50) {
//                return false;
//            }
//
//            // Check SUPPLY
//            if (AGame.getSupplyFree() == 0) {
//                return false;
//            }
//        }
//
//        // =========================================================
//        // Check PRODUCTION QUEUE
//        ArrayList<ProductionOrder> unitsToProduce = AGame.getBuildOrders().getThingsToProduceRightNow(
//                true);
//
//        // EMPTY PRODUCTION QUEUE - can build
//        if (unitsToProduce.isEmpty()) {
//            return true;
//        } // Production queue not empty
//        else {
//            for (ProductionOrder order : unitsToProduce) {
//                if (order.getUnitType().isOrganic()) {	//replaces  isInfantry()
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }

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
        if (AGame.playsAsZerg()) {
            return Select.ourLarva().count() > 0;
        } else {
            return barracks.getTrainingQueue().size() == 0;
        }
    }

}
