
package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.List;


public class ZergDynamicUnitsManager {

    public static void update() {
        zerglingsIfNeeded();
    }

    // =========================================================

    private static void zerglingsIfNeeded() {
        if (!AGame.canAffordWithReserved(200, 0)) {
            return;
        }

        List<AUnit> eggs = Select.ourOfType(AUnitType.Zerg_Egg).listUnits();
        if (eggs.size() >= 3) {
            produceZergUnit(AUnitType.Zerg_Zergling);
        }
    }

    // =========================================================

    private static void produceZergUnit(AUnitType unitType) {
        for (AUnit base : Select.ourBases().reverse().list()) {
            if (!base.isTrainingAnyUnit()) {
                base.train(unitType);
                return;
            }
        }
    }

}
