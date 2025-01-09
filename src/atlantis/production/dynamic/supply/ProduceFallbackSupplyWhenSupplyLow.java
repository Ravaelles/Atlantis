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
        int bonus = A.minerals() >= 350 ? 1 : 0;

        int supplyUsed = A.supplyUsed();
        if (supplyUsed <= 14) return 0 + bonus;
        if (supplyUsed <= 28) return 3 + bonus;
        if (supplyUsed <= 40) return 4 + bonus;
        if (supplyUsed <= 60) return 6 + bonus;
        if (supplyUsed <= 90) return 8 + bonus;
        if (supplyUsed <= 120) return 11 + bonus;
        if (supplyUsed <= 190) return 14 + bonus;
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
