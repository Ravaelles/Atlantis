package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.strategy.OurStrategy;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.add.PreventDuplicateOrders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Protoss_Assimilator;
import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceFirstAssimilator {
    public static boolean produce() {
        if (Have.assimilator()) return false;
//        if (CountInQueue.count(Protoss_Assimilator) > 0) return;

        if (OurStrategy.get().isExpansion() && A.supplyUsed() <= 44) return false;
        if (!Have.existingOrUnfinished(Protoss_Cybernetics_Core)) return false;

        ProductionOrder existingOrder = Queue.get().nonCompletedNext30().ofType(type()).first();
        if (existingOrder != null && existingOrder.requestedAgo() >= 30 * 10) {
            A.errPrintln("Canceling existing ASSIM order " + existingOrder);
            PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(type());
        }

//        if (!A.hasMinerals(300) && Count.ourUnfinishedOfType(Protoss_Cybernetics_Core) == 0) return;
        if (Count.ourWithUnfinished(Protoss_Cybernetics_Core) >= 1) {
            return AddToQueue.withTopPriority(type()) != null
                && A.errPrintln("FORCE ABC Assimilator to queue at " + A.minSec());
            //        DynamicCommanderHelpers.buildToHaveOne(A.supplyUsed() - 2, Protoss_Assimilator);
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Assimilator;
    }
}
