package atlantis.production;

import atlantis.AGame;
import atlantis.constructing.AConstructionRequests;
import atlantis.production.orders.ProductionQueue;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Helpers;

public class AbstractDynamicUnits extends Helpers {

    protected static void trainIfPossible(int minSupply, AUnitType type, boolean onlyOneAtTime) {
        if (noSupply(minSupply)) {
            return;
        }

        trainIfPossible(type, onlyOneAtTime, type.getMineralPrice(), type.getGasPrice());
    }

    protected static void trainIfPossible(AUnitType type, boolean onlyOneAtTime) {
        trainIfPossible(type, onlyOneAtTime, 0, 0);
    }

    protected static void trainIfPossible(AUnitType type, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!AGame.canAfford(hasMinerals, hasGas)) {
            return;
        }

        if (onlyOneAtTime && AConstructionRequests.hasRequestedConstructionOf(type)) {
            return;
        }

        AUnitType building = type.getWhatBuildsIt();
        for (AUnit buildingProducing : Select.ourOfType(building).listUnits()) {
            if (!buildingProducing.isTrainingAnyUnit()) {
                buildingProducing.train(type);
                return;
            }
        }
    }
    
    protected static void trainNowIfHaveWhatsRequired(AUnitType type, boolean onlyOneAtTime) {
        if (!onlyOneAtTime) {
            AGame.exit("Unhandled yet");
        }

        AUnitType building = type.getWhatBuildsIt();
        if (Count.ofType(building) == 0) {
            return;
        }

        if (onlyOneAtTime && Count.ourOfTypeIncludingUnfinished(type) > 0) {
            return;
        }

        if (ProductionQueue.isAtTopOfProductionQueue(type, 3)) {
            return;
        }
        
        trainNow(AUnitType.Protoss_Arbiter, onlyOneAtTime);
    }
    
    protected static void trainNow(AUnitType type, boolean onlyOneAtTime) {
        ProductionQueue.addWithTopPriority(type);
    }

}
