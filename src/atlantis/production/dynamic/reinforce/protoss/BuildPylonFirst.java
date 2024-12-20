package atlantis.production.dynamic.reinforce.protoss;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.constructing.position.RequestBuildingNear;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Pylon;

public class BuildPylonFirst {
    public static String lastError = null;

    public static ProductionOrder requestNear(HasPosition position) {
//        System.err.println(Count.inQueue(Protoss_Pylon));
//        System.err.println(Count.inProduction(Protoss_Pylon));
//        System.err.println("Count.inQueueOrUnfinished(type(), 15) = " + Count.inQueueOrUnfinished(type(), 15));

        if (Count.inQueueOrUnfinished(type(), 15) >= 5) return error("Too many Pylonz in queue");
        if (Count.notFinishedConstructions(type(), 5, position) > 0) return error("Have pending pylon near");

//        ProductionOrder order = AddToQueue.withHighPriority(Protoss_Pylon, position);
//        order.setAroundPosition(position);
//        order.markAsUsingExactPosition();
        ProductionOrder order = requestPylon(position);
        if (order == null) return error("Failed to request pylon - order is null");

//        Construction construction = NewConstructionRequest.requestConstructionOf(type(), position, order);
        Construction construction = order.construction();
        if (construction == null) {
            order.cancel();
            return error("Failed to request pylon - construction is null");
        }

        lastError = null;
        return order;
    }

    public static boolean needsPylon(HasPosition position) {
        return Select.ourOfType(type()).inRadius(5.9, position).count() == 0;
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
        return null;
    }
}
