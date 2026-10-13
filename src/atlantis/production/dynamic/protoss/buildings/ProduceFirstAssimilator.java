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

import atlantis.util.AConsole;
import static atlantis.units.AUnitType.Protoss_Assimilator;
import static atlantis.units.AUnitType.Protoss_Cybernetics_Core;

public class ProduceFirstAssimilator {
    public static String reason = "-";

    public static boolean produce() {
        reason = "Eligible";
        if (Have.assimilator()) return blocked("AlreadyHaveAssimilator");
        if (CountInQueue.count(type(), 2) > 0) return blocked("AlreadyQueued");
        if (Strategy.get().isExpansion() && A.supplyUsed() <= 44) return blocked("ExpansionSupplyGate");

        AUnit cc;
        if ((cc = Select.ourWithUnfinished(Protoss_Cybernetics_Core).first()) == null)
            return blocked("NoCyberneticsCore");
        if (cc.getRemainingBuildTimeInSeconds() >= 80) return blocked("CyberneticsCoreNotNearCompletion");


        if (cc != null) {
            Queue.get().notStarted().ofType(Protoss_Assimilator).cancelAll("Force Assimilator with CC");


            // Was: `AddToQueue... != null && AConsole.errPrintln(...)`, i.e. the
            // print helper's `true` return silently doubled as the method result.
            // Written out, so the queue decision and the logging are separate.
            if (AddToQueue.withTopPriority(type()) != null) {
                reason = "Queued";
                AConsole.errPrintln("FORCE added first Assimilator to queue at " + A.minSec());
                return true;
            }
            return blocked("QueueRejected");
            //        DynamicCommanderHelpers.buildToHaveOne(A.supplyUsed() - 2, Protoss_Assimilator);
        }

        return blocked("NoCyberneticsCore");
    }

    private static boolean blocked(String reason) {
        ProduceFirstAssimilator.reason = reason;
        return false;
    }

    private static AUnitType type() {
        return Protoss_Assimilator;
    }
}
