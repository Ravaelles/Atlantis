
package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

import java.util.List;


public class ProtossDynamicUnitsManager {

    public static void update() {
        scarabsIfNeeded();
        dragoonsIfNeeded();
    }

    // =========================================================

    private static void dragoonsIfNeeded() {
        if (AGame.getSupplyTotal() < 20) {
            return;
        }

        trainIfPossible(AUnitType.Protoss_Dragoon, 200, 100);
    }

    private static void scarabsIfNeeded() {
        List<AUnit> reavers = Select.ourOfType(AUnitType.Protoss_Reaver).listUnits();
        for (AUnit reaver : reavers) {
            if (reaver.getScarabCount() <= 2 && !reaver.isTrainingAnyUnit()) {
                reaver.train(AUnitType.Protoss_Scarab);
            }
        }
    }

    private static void trainIfPossible(AUnitType unitType, int hasMinerals, int hasGas) {
        if (!AGame.canAfford(hasMinerals, hasGas)) {
            return;
        }

        for (AUnit buildingProducing : Select.ourOfType(unitType).listUnits()) {
            if (!buildingProducing.isTrainingAnyUnit()) {
                buildingProducing.train(unitType);
                return;
            }
        }
    }

}
