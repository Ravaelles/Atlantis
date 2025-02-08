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
        if (A.supplyFree() > minFreeSupplyToAct()) return false;
        if (A.supplyUsed() < 8) return false;
        if (A.everyFrameExceptNthFrame(19)) return false;
//        if (Count.pylonsWithUnfinished() > 0) return false;
        if (ConstructionRequests.countNotFinishedOfType(type()) >= (1 + (A.minerals() / 150))) return false;
        if (CountInQueue.count(type(), 5) >= (1 + (A.minerals() / 150))) return false;

        return produceOne();
    }

    public AUnitType type() {
        return Protoss_Pylon;
    }
}
