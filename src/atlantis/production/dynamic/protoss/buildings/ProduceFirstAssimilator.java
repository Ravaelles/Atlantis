package atlantis.production.dynamic.protoss.buildings;

import atlantis.game.A;
import atlantis.information.strategy.Strategy;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.RemoveFromQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.add.PreventDuplicateOrders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Assimilator;
import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceFirstAssimilator {
    public static boolean produce() {
        if (Have.assimilator()) return false;
        if (CountInQueue.count(type(), 2) > 0) return false;
        if (Strategy.get().isExpansion() && A.supplyUsed() <= 44) return false;
//        if (!Have.existingOrUnfinished(Protoss_Cybernetics_Core)) return false;

        AUnit cc;
        if ((cc = Select.ourWithUnfinished(Protoss_Cybernetics_Core).first()) == null) return false;
//        System.err.println("cc.getRemainingBuildTimeInSeconds() = " + cc.getRemainingBuildTimeInSeconds());
        if (cc.getRemainingBuildTimeInSeconds() >= 80) return false;

//        ProductionOrder existingOrder = Queue.get().notFinishedNext30().ofType(type()).first();
//        if (existingOrder != null && existingOrder.requestedAgo() >= 30 * 10) {
//            A.errPrintln("Canceling existing ASSIM order " + existingOrder);
//            PreventDuplicateOrders.cancelPreviousNonStartedOrdersOf(
//                type(), "Assim takes long (" + (existingOrder.requestedAgo() / 30) + "s)"
//            );
//        }

//        if (!A.hasMinerals(300) && Count.ourUnfinishedOfType(Protoss_Cybernetics_Core) == 0) return;
//        if (Count.ourWithUnfinished(Protoss_Cybernetics_Core) >= 1) {
        if (cc != null) {
            Queue.get().notStarted().ofType(Protoss_Assimilator).cancelAll("Force Assimilator with CC");

//            RemoveFromQueue.removeBuildingOrdersThatDontHaveConstructionYetSoTheyAreNotStarted(type());

            return AddToQueue.withTopPriority(type()) != null
                && A.errPrintln("FORCE added first Assimilator to queue at " + A.minSec());
            //        DynamicCommanderHelpers.buildToHaveOne(A.supplyUsed() - 2, Protoss_Assimilator);
        }

        return false;
    }

    private static AUnitType type() {
        return Protoss_Assimilator;
    }
}
