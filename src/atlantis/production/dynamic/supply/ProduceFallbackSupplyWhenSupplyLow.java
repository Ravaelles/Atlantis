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

        // Restored 2026-10-13 to the old ProtossSupplyCommander bands (owner
        // ask: Pylons much earlier, like ASupplyManager used to). The lazier
        // interim table (0/3/4/6/8/11/14) capped games that the old 2/4/5/8/10
        // /14/19 rode through - measured against its deleted source. Axis and
        // in-flight caps below are unchanged; only the trigger levels moved.
        int supplyTotal = A.supplyTotal();
        if (supplyTotal <= 11) return 2 + bonus;
        if (supplyTotal <= 20) return 4 + bonus;
        if (supplyTotal <= 36) return 5 + bonus;
        if (supplyTotal <= 46) return 8 + bonus;
        if (supplyTotal <= 70) return 10 + bonus;
        if (supplyTotal <= 120) return 14 + bonus;
        if (supplyTotal <= 170) return 14 + bonus;
        if (supplyTotal <= 200) return 19 + bonus;
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
