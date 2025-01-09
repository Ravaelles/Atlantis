package atlantis.production.dynamic.reinforce.protoss;

import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.RequestBuildingNear;
import atlantis.production.orders.production.queue.QueueLastStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

import static atlantis.units.AUnitType.Protoss_Pylon;

public class BuildPylonFirst {
    public static String lastError = null;

    public static ProductionOrder requestNear(HasPosition position) {
//        System.err.println(Count.inQueue(Protoss_Pylon));
//        System.err.println(Count.inProduction(Protoss_Pylon));
//        System.err.println("Count.inQueueOrUnfinished(type(), 15) = " + Count.inQueueOrUnfinished(type(), 15));

//        if (Count.inQueueOrUnfinished(type(), 15) >= 5) return error("Too many Pylonz in queue");
        if (Count.notFinishedConstructions(type(), 5.5, position) > 0) {
//            ErrorLog.debug("Have pending pylon near");
            return error("Have pending pylon near");
        }

//        ProductionOrder order = AddToQueue.withHighPriority(Protoss_Pylon, position);
//        order.setAroundPosition(position);
//        order.markAsUsingExactPosition();
        ProductionOrder order = requestPylon(position);
        if (order == null) return error(
            "Request Pylon fail - null order - " + lastError + " / queue_status:" + QueueLastStatus.status()
        );

//        System.out.println("----- Requested PYLON ORDER at " + order.aroundPosition() + " / " + position);

//        Construction construction = NewConstructionRequest.requestConstructionOf(type(), position, order);
        Construction construction = order.construction();
        if (construction == null) {
            order.cancel("Invalid state - construction can't be determined");
            return error("Failed to request pylon - construction is null");
        }

//        System.out.println("----- Requested PYLON CONSTRUCTION at " + order.aroundPosition() + " / " + position);

        lastError = null;
        return order;
    }

    public static boolean needsPylon(HasPosition position) {
        return Select.ourOfType(type()).inRadius(5.3, position).count() == 0;
    }

    // =========================================================

    private static AUnitType type() {
        return Protoss_Pylon;
    }

    private static ProductionOrder requestPylon(HasPosition aroundPosition) {
        return RequestBuildingNear.constructionOf(AUnitType.Protoss_Pylon)
            .near(aroundPosition)
            .maxDistance(10)
            .specialGridExclusionPermission()
            .maxOtherUnfinishedNearby(0)
            .request();
    }

    // =========================================================

    private static ProductionOrder error(String error) {
        lastError = error;
//        ErrorLog.debug(error);
//        ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(error);
        return null;
    }
}
