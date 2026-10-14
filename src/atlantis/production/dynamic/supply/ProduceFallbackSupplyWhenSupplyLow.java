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
        // ask: Pylons much earlier, like ASupplyManager used to). Widened
        // 2026-10-14 by one warp of headroom (owner ask: anticipate 8-10
        // ahead from supply 30): measured on Wine that a Pylon ordered <240f
        // before the cap still blocks 11-37s WITH bank, because warp alone is
        // ~600f. The trigger is the order moment, but the need is the
        // completion moment - so mid/late bands carry ~2-4 extra free supply
        // (≈ one warp of worker output). Opening bands unchanged (BO/PylonAt8
        // own the start); in-flight caps below are unchanged.
        int supplyTotal = A.supplyTotal();
        if (supplyTotal <= 11) return 2 + bonus;
        if (supplyTotal <= 20) return 4 + bonus;
        if (supplyTotal <= 36) return 8 + bonus;
        if (supplyTotal <= 46) return 10 + bonus;
        if (supplyTotal <= 70) return 12 + bonus;
        if (supplyTotal <= 120) return 16 + bonus;
        if (supplyTotal <= 170) return 16 + bonus;
        if (supplyTotal <= 200) return 22 + bonus;
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
