package atlantis.production.dynamic.supply;

import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Protoss_Pylon;

public class ProduceFallbackPylonWhenSupplyLow extends ProduceFallbackSupplyWhenSupplyLow {
    @Override
    public boolean shouldProduce() {
//        if (A.supplyTotal() >= 15) return false;
        // Overriding criterion first (owner): one Pylon at a time, free
        // supply within the band for the current game stage. Skips all
        // band math when no provider is needed.
        if (!NeedDynamicSupply.forCurrentRace().needsSupply(
            A.supplyUsed(), A.supplyTotal(),
            ConstructionRequests.countNotFinishedOfType(type()))) return false;
        if (A.supplyFree() > minFreeSupplyToAct()) return false;
        if (A.supplyUsed() < 8) return false;
        if (A.everyFrameExceptNthFrame(19)) return false;
//        if (Count.pylonsWithUnfinished() > 0) return false;
        // Pipeline overlap (owner doctrine: the next Pylon goes out BEFORE
        // the previous completes, so supply never sits at the cap waiting
        // for a warp). Measured on Wine: single-pipeline (cap 1 below 150
        // bank) serialized P4->P5 with a 40/41 stall between. Two in flight
        // max while poor, bank/150 more above that - affordability still
        // gates every order, this only stops the cap from vetoing the second.
        if (ConstructionRequests.countNotFinishedOfType(type()) >= (2 + (A.minerals() / 150))) return false;
        if (CountInQueue.count(type(), 5) >= (2 + (A.minerals() / 150))) return false;

        return produceOne();
    }

    public AUnitType type() {
        return Protoss_Pylon;
    }
}
