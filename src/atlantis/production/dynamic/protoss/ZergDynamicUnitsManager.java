
package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.production.AbstractDynamicUnits;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;


public class ZergDynamicUnitsManager extends AbstractDynamicUnits {

    public static void update() {
        mutalisks();
        hydralisks();
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

        if (larvas(1) && AGame.canAffordWithReserved(50, 0)) {
            AddToQueue.withStandardPriority(AUnitType.Zerg_Mutalisk);
        }
    }

    private static boolean hydralisks() {
        if (Have.no(AUnitType.Zerg_Hydralisk_Den)) {
            return false;
        }

        if (AGame.canAffordWithReserved(50, 0)) {
            AddToQueue.withStandardPriority(AUnitType.Zerg_Hydralisk);
            return true;
        }

        return false;
    }

    private static boolean zerglings() {
        if (Have.no(AUnitType.Zerg_Spawning_Pool)) {
            return false;
        }

        if (!Decisions.shouldMakeZerglings()) {
            return false;
        }

        if (Have.hydraliskDen()) {
            if (!A.hasMinerals(210)) {
                return false;
            }
        }

        if (Count.zerglings() <= 50 && larvas(1) && A.hasMinerals(50)) {
            System.err.println(A.now() + " zetgling enqueued");
            AddToQueue.withStandardPriority(AUnitType.Zerg_Zergling);
            return true;
        }

        return false;
    }

    // =========================================================

//    private static void make(AUnitType type) {
//        CurrentBuildOrder.get().produceUnit(type);
////        for (AUnit base : Select.ourBases().reverse().list()) {
////            if (!base.isTrainingAnyUnit()) {
//////                base.train(unitType);
////                return;
////            }
////        }
//    }

    private static boolean larvas(int minLarvas) {
        return Select.ourLarva().count() >= minLarvas;
    }

}
