
package atlantis.production.dynamic.protoss;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.production.AbstractDynamicUnits;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;


public class ZergDynamicUnitsManager extends AbstractDynamicUnits {

    public void handle() {
        mutalisks();
        hydras();
        zerglings();
    }

    // =========================================================

    private static void mutalisks() {
        if (!Have.a(AUnitType.Zerg_Spire) && !Have.a(AUnitType.Zerg_Greater_Spire)) {
            return;
        }

        int mutas = Count.mutas();

        if (mutas <= 2) {
            AddToQueue.withHighPriority(AUnitType.Zerg_Mutalisk);
            AddToQueue.withHighPriority(AUnitType.Zerg_Mutalisk);
            AddToQueue.withHighPriority(AUnitType.Zerg_Mutalisk);
        }
        else {
            if (!AGame.canAffordWithReserved(75, 75)) {
                return;
            }

            if (larvas(1) && AGame.canAffordWithReserved(50, 0)) {
                AddToQueue.withStandardPriority(AUnitType.Zerg_Mutalisk);
            }
        }
    }

    private static boolean hydras() {
        if (!Have.a(AUnitType.Zerg_Hydralisk_Den)) {
            return false;
        }

        int hydras = Count.hydralisks();

        if (larvas() == 0) {
            return false;
        }

        if (hydras <= 2 || AGame.canAffordWithReserved(50, 0)) {
            AddToQueue.withStandardPriority(AUnitType.Zerg_Hydralisk);
            return true;
        }

        return false;
    }

    private static boolean zerglings() {
        if (!Have.a(AUnitType.Zerg_Spawning_Pool)) {
            return false;
        }

        int zerglings = Count.zerglings();

        if (zerglings >= 2 && larvas() == 0) {
            return false;
        }

        if (Have.hydraliskDen()) {
            if (!A.hasMinerals(210) && zerglings >= 4) {
                return false;
            }
        }

        if (!Decisions.shouldMakeZerglings()) {
            return false;
        }

        if (
            zerglings <= 50
                && larvas(1)
                && (zerglings <= 3 || AGame.canAffordWithReserved(50, 0))
        ) {
//            System.err.println(A.now() + " zergling enqueued");
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
        return Count.larvas() >= minLarvas;
    }

    private static int larvas() {
        return Count.larvas();
    }

}
