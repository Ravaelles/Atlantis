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
        // CAPPED 2026-10-19 (owner order, Wine 38/65 = 27 free): free supply
        // never exceeds 12-14, so every band triggers at 6 or below (a Pylon
        // lands +8). Reverts the 2026-10-14 anticipation; a fast pump can now
        // outrun the 600f warp and block - the accepted tradeoff, named live
        // by the SUPPLY_BLOCKED twin. Cross-author duplicates are NOT fixed
        // here (WO-144 single author is the real fix, still open).
        int band;
        if (supplyTotal <= 11) band = 2 + bonus;
        else if (supplyTotal <= 20) band = 4 + bonus;
        else if (supplyTotal <= 36) band = 8 + bonus;
        else if (supplyTotal <= 46) band = 10 + bonus;
        else if (supplyTotal <= 70) band = 12 + bonus;
        else if (supplyTotal <= 120) band = 16 + bonus;
        else if (supplyTotal <= 170) band = 16 + bonus;
        else if (supplyTotal <= 200) band = 22 + bonus;
        else band = -1;
        return band < 0 ? -1 : Math.min(6, band);
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
