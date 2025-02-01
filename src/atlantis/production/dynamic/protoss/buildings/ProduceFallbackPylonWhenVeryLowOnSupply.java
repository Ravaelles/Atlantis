package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Protoss_Pylon;

public class ProduceFallbackPylonWhenVeryLowOnSupply {
    public static boolean produce() {
//        if (A.supplyTotal() >= 15) return false;
        if (A.supplyFree() > minFreeSupplyToAct()) return false;
        if (A.supplyUsed() < 8) return false;
        if (A.everyFrameExceptNthFrame(19)) return false;
//        if (Count.pylonsWithUnfinished() > 0) return false;
        if (ConstructionRequests.countNotFinishedOfType(type()) >= (1 + (A.minerals() / 150))) return false;
        if (CountInQueue.count(type(), 5) >= (1 + (A.minerals() / 150))) return false;

        return producePylon();
    }

    private static int minFreeSupplyToAct() {
        int supplyUsed = A.supplyUsed();
        if (supplyUsed <= 14) return 0;
        if (supplyUsed <= 28) return 2;
        if (supplyUsed <= 40) return 3;
        if (supplyUsed <= 60) return 5;
        if (supplyUsed <= 90) return 6;
        if (supplyUsed <= 120) return 10;
        if (supplyUsed <= 190) return 14;
        return -1;
    }

    private static boolean producePylon() {
        ProductionOrder order = AddToQueue.withTopPriority(type());
        if (order != null) order.setMinSupply(A.supplyUsed() - 2);

//        ErrorLog.printMaxOncePerMinute(
//            A.minSec() + " @@@@@@@@@@@@ Fallback: Enforce Pylon (" + A.supplyUsed() + "/" + A.supplyTotal() + "): "
//                + A.trueFalse(order != null)
//        );

        return order != null;
    }

    private static AUnitType type() {
        return Protoss_Pylon;
    }
}
