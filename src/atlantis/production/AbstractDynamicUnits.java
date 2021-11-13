package atlantis.production;

import atlantis.AGame;
import atlantis.production.constructing.AConstructionRequests;
import atlantis.production.orders.AddToQueue;
import atlantis.production.orders.ProductionQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Helpers;

public class AbstractDynamicUnits extends Helpers {

    protected static boolean addToQueue(AUnitType type) {
        if (AGame.supplyFree() == 0) {
            return false;
        }

        if (!AGame.canAffordWithReserved(Math.max(80, type.getMineralPrice()), type.getGasPrice())) {
            return false;
        }

        return AddToQueue.withStandardPriority(type);
    }

    // =========================================================

    protected static void buildToHave(AUnitType type, int haveN) {
        if (Count.includingPlanned(type) < haveN) {
            trainIfPossible(type);
        }
    }

    protected static boolean trainIfPossible(int minSupply, AUnitType type, boolean onlyOneAtTime) {
        if (noSupply(minSupply)) {
            return false;
        }

        return trainIfPossible(type, onlyOneAtTime, type.getMineralPrice(), type.getGasPrice());
    }

    protected static boolean trainIfPossible(AUnitType type) {
        return trainIfPossible(type, false, type.getMineralPrice(), type.getGasPrice());
    }

    protected static boolean trainIfPossible(AUnitType type, boolean onlyOneAtTime) {
        return trainIfPossible(type, onlyOneAtTime, 0, 0);
    }

    protected static boolean trainIfPossible(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
//        if (!AGame.canAfford(hasMinerals, hasGas)) {
        if (!AGame.canAffordWithReserved(hasMinerals, hasGas)) {
            return false;
        }

        if (onlyOneAtTime) {
            if (type.isBuilding() && AConstructionRequests.hasRequestedConstructionOf(type)) {
                return false;
            }
        }

        return addToQueueIfHaveFreeBuilding(type);
    }

    protected static void trainNowIfHaveWhatsRequired(AUnitType type, boolean onlyOneAtTime) {
        if (!onlyOneAtTime) {
            AGame.exit("Unhandled yet");
        }

        if (!Requirements.hasRequirements(type)) {
            return;
        }

//        AUnitType building = type.getWhatBuildsIt();
//        if (Count.ofType(building) == 0) {
//            return;
//        }
//
//        if (onlyOneAtTime && Count.ourOfTypeIncludingUnfinished(type) > 0) {
//            return;
//        }

        if (ProductionQueue.isAtTheTopOfQueue(type, 8)) {
            return;
        }

        addToQueueIfHaveFreeBuilding(type);
    }
    
//    protected static void trainNow(AUnitType type) {
//        AddToQueue.withTopPriority(type);
//    }
//
//    protected static void trainNow(AUnitType type, boolean onlyOneAtTime) {
//        AddToQueue.withTopPriority(type);
//    }

    protected static boolean addToQueueIfHaveFreeBuilding(AUnitType type) {
        AUnitType building = type.whatBuildsIt();
        for (AUnit buildingProducing : Select.ourOfType(building).listUnits()) {
            if (!buildingProducing.isTrainingAnyUnit()) {
//                trainNow(type);
                addToQueue(type);
                return true;
            }
        }
        return false;
    }

}
