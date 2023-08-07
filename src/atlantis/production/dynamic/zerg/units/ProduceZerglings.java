package atlantis.production.dynamic.zerg.units;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.information.decisions.Decisions;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class ProduceZerglings {
    public static boolean zerglings() {
        if (!Have.a(AUnitType.Zerg_Spawning_Pool)) {
            return false;
        }

        int zerglings = Count.zerglings();
        int larvas = Count.larvas();

        if (zerglings >= 2 && larvas == 0) {
            return false;
        }

        if (Have.hydraliskDen()) {
            if (!A.hasMinerals(210) && zerglings >= 4) {
                return false;
            }
        }

        if (zerglings <= 5) {
            return produce();
        }

        if (larvas <= (A.seconds() <= 200 ? 1 : 2)) return false;

        if (AGame.supplyFree() <= 1 || !AGame.canAffordWithReserved(150, 0)) return false;

        if (
            zerglings <= 40 && (AGame.canAffordWithReserved(50, 0))
        ) {
//            System.err.println(A.now() + " zergling enqueued");
            return produce();
        }

        return false;

    }

    private static boolean produce() {
        AddToQueue.withStandardPriority(AUnitType.Zerg_Zergling);
        return true;
    }
}
