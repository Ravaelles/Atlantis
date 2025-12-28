package atlantis.production.dynamic.protoss.reinforce;

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
            .maxDistance(8)
            .specialGridExclusionPermission()
            .maxOtherUnfinishedNearby(2)
            .request();
    }
}
