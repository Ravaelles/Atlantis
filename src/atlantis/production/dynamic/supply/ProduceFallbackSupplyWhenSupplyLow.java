package atlantis.production.dynamic.supply;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public abstract class ProduceFallbackSupplyWhenSupplyLow {
    public abstract boolean shouldProduce();

    public abstract AUnitType type();

    public boolean produceIfNeeded() {
        return shouldProduce() && produceOne();
    }

    protected int minFreeSupplyToAct() {
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

    protected boolean produceOne() {
        ProductionOrder order = AddToQueue.withTopPriority(type());
        if (order != null) order.setMinSupply(A.supplyUsed() - 2);

//        ErrorLog.printMaxOncePerMinute(
//            A.minSec() + " @@@@@@@@@@@@ Fallback: Enforce SUPPLY (" + A.supplyUsed() + "/" + A.supplyTotal() + "): "
//                + A.trueFalse(order != null)
//        );

        return order != null;
    }
}
