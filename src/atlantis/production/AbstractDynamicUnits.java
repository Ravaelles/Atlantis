package atlantis.production;

import atlantis.AGame;
import atlantis.constructing.AConstructionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

public class AbstractDynamicUnits {

    protected static void trainIfPossible(AUnitType unitType, boolean onlyOneAtTime, int hasMinerals, int hasGas) {
        if (!AGame.canAfford(hasMinerals, hasGas)) {
            return;
        }

        if (onlyOneAtTime && AConstructionManager.hasRequestedConstructionOf(unitType)) {
            return;
        }

        AUnitType building = unitType.getWhatBuildsIt();
        for (AUnit buildingProducing : Select.ourOfType(building).listUnits()) {
            if (!buildingProducing.isTrainingAnyUnit()) {
                buildingProducing.train(unitType);
                return;
            }
        }
    }

}
