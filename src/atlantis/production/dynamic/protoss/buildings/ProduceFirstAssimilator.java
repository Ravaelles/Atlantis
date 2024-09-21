package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.add.PreventDuplicateOrders;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Assimilator;
import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceFirstAssimilator {
    public static void produce() {
        if (A.gas() > 0 || Have.assimilator()) return;
//        if (CountInQueue.count(Protoss_Assimilator) > 0) return;

        PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(type());

//        if (!A.hasMinerals(300) && Count.ourUnfinishedOfType(Protoss_Cybernetics_Core) == 0) return;
        if (Count.ourUnfinishedOfType(Protoss_Cybernetics_Core) >= 1) {
            AddToQueue.withTopPriority(type());
            //        DynamicCommanderHelpers.buildToHaveOne(A.supplyUsed() - 2, Protoss_Assimilator);
        }
    }

    private static AUnitType type() {
        return Protoss_Assimilator;
    }
}
