
package atlantis.production.dynamic.protoss;

import atlantis.game.AGame;
import atlantis.production.AbstractDynamicUnits;
import atlantis.production.orders.CurrentBuildOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;


public class ZergDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        mutalisks();
        zerglings();
    }

    // =========================================================

    private static void mutalisks() {
        if (Have.no(AUnitType.Zerg_Spire) && Have.no(AUnitType.Zerg_Greater_Spire)) {
            return;
        }

        if (!AGame.canAffordWithReserved(75, 75)) {
            return;
        }

        if (larvas(1)) {
            make(AUnitType.Zerg_Mutalisk);
        }
    }

    private static void zerglings() {
        if (!AGame.canAffordWithReserved(100, 0)) {
            return;
        }

        if (larvas(2)) {
            make(AUnitType.Zerg_Zergling);
        }
    }

    // =========================================================

    private static void make(AUnitType type) {
        CurrentBuildOrder.get().produceUnit(type);
//        for (AUnit base : Select.ourBases().reverse().list()) {
//            if (!base.isTrainingAnyUnit()) {
////                base.train(unitType);
//                return;
//            }
//        }
    }

    private static boolean larvas(int minLarvas) {
        return Select.ourLarva().count() >= minLarvas;
    }

}
