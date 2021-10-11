
package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

import java.util.List;


public class ProtossDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        scarabs();
        dragoons();
        arbiters();
    }

    // =========================================================

    private static void dragoons() {
        trainIfPossible(17, AUnitType.Protoss_Dragoon, false);
    }

    private static void scarabs() {
        List<AUnit> reavers = Select.ourOfType(AUnitType.Protoss_Reaver).listUnits();
        for (AUnit reaver : reavers) {
            if (reaver.getScarabCount() <= 2 && !reaver.isTrainingAnyUnit()) {
                reaver.train(AUnitType.Protoss_Scarab);
            }
        }
    }

    private static void arbiters() {
        trainIfPossible(95, AUnitType.Protoss_Arbiter, true);
    }

}
