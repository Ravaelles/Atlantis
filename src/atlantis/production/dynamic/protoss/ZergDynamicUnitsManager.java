
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
            System.out.println("mutas = " + mutas);
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
        return Select.ourLarva().count() >= minLarvas;
    }

}
