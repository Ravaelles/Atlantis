
package atlantis.production.dynamic.zerg;

import atlantis.architecture.Commander;
import atlantis.game.AGame;
import atlantis.production.dynamic.zerg.units.ProduceHydras;
import atlantis.production.dynamic.zerg.units.ProduceZerglings;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.We;

public class ZergDynamicUnitsCommander extends Commander {
    @Override
    public boolean applies() {
        return We.zerg();
    }

    @Override
    protected void handle() {
        mutalisks();
        ProduceHydras.hydras();
        ProduceZerglings.zerglings();
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
