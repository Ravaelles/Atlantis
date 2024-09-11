package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.DynamicCommanderHelpers;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Assimilator;
import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceFirstAssimilator {
    public static void produce() {
        if (A.gas() > 0 || Have.assimilator()) return;
        if (CountInQueue.count(Protoss_Assimilator) > 0) return;
        if (
            !Have.cyberneticsCore() && ConstructionRequests.countNotFinishedOfType(Protoss_Cybernetics_Core) == 0
        ) return;

        DynamicCommanderHelpers.buildToHaveOne(A.supplyUsed() - 2, Protoss_Assimilator);
    }
}
