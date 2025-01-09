package atlantis.production.dynamic.reinforce.protoss;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.RequestBuildingNear;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;

public class RequestCannonAt {
    public static ProductionOrder at(HasPosition position) {
//        AUnit existingPylon = Select.ourOfType(Protoss_Pylon).inRadius(10, position).nearestTo(position);

//        System.out.println(
//            A.minSec() + " ------- BuildPylonFirst (" + BuildPylonFirst.needsPylon(position) + ") at " + position
//        );

        if (BuildPylonFirst.needsPylon(position)) {
            return BuildPylonFirst.requestNear(position);
        }

        return requestCannon(position);
    }

    private static ProductionOrder requestCannon(HasPosition aroundPosition) {
        return RequestBuildingNear.constructionOf(AUnitType.Protoss_Photon_Cannon)
            .near(aroundPosition)
            .maxDistance(10)
            .specialGridExclusionPermission()
            .maxOtherUnfinishedNearby(2)
            .request();
    }

//    public static boolean at(HasPosition position) {
////        AUnit existingPylon = Select.ourOfType(Protoss_Pylon).inRadius(10, position).nearestTo(position);
//
//        if (Select.ourOfType(Protoss_Pylon).inRadius(5.9, position).count() == 0) {
//            if (buildPylonFirst(position)) return true;
//        }
//
//        return requestCannon(position);
//    }
//
//    private static boolean requestCannon(HasPosition position) {
////        AddToQueue.withHighPriority(Protoss_Photon_Cannon, position);
//
////        AUnit nearestPylon = Select.ourOfType(Protoss_Pylon).nearestTo(position);
////        if (nearestPylon != null) {
////            A.println("@@@@@@@@@@ Requesting cannon at " + position + ", nearest pylon dist: " + nearestPylon.distTo(position));
////        }
//
//        ProductionOrder order = AddToQueue.withHighPriority(Protoss_Photon_Cannon, position);
//        return NewConstructionRequest.requestConstructionOf(Protoss_Photon_Cannon, position, order) != null;
//    }
//
//    private static boolean buildPylonFirst(HasPosition position) {
//        if (Count.inQueueOrUnfinished(Protoss_Pylon, 99) >= 5) return false;
//        if (Count.existingOrPlannedBuildingsNear(Protoss_Pylon, 5, position) > 0) return false;
//
//        ProductionOrder order = AddToQueue.withHighPriority(Protoss_Pylon, position);
//        return NewConstructionRequest.requestConstructionOf(Protoss_Pylon, position, order) != null;
//    }
}
