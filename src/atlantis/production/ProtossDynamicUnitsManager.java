
package atlantis.production;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Count;
import atlantis.units.Select;

import java.util.List;


public class ProtossDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        if (AGame.notNthGameFrame(3)) {
            return ;
        }

        scarabs();
        dragoons();
        arbiters();
        zealots();
    }

    // =========================================================

    private static void dragoons() {
        trainIfPossible(17, AUnitType.Protoss_Dragoon, false);
    }

    private static void zealots() {
        if (AGame.isEnemyZerg() && Count.ofType(AUnitType.Protoss_Zealot) <= 0) {
            trainIfPossible(11, AUnitType.Protoss_Zealot, true);
        }
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
        trainNowIfHaveWhatsRequired(AUnitType.Protoss_Arbiter, true);
    }

}
