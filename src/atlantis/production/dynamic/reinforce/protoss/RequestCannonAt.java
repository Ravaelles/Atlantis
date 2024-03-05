package atlantis.production.dynamic.reinforce.protoss;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;
import static atlantis.units.AUnitType.Protoss_Pylon;

public class RequestCannonAt {
    public static boolean at(HasPosition position) {
        if (Select.ourOfType(Protoss_Pylon).inRadius(6, position).count() == 0) {
            if (buildPylonFirst(position)) return true;
        }

        return requestCannon(position);
    }

    private static boolean requestCannon(HasPosition position) {
//        AddToQueue.withHighPriority(Protoss_Photon_Cannon, position);

        ProductionOrder order = AddToQueue.withHighPriority(Protoss_Photon_Cannon, position);
        return NewConstructionRequest.requestConstructionOf(Protoss_Photon_Cannon, position, order);
    }

    private static boolean buildPylonFirst(HasPosition position) {
        if (Count.inQueueOrUnfinished(Protoss_Pylon, 99) >= 3) return false;
        if (Count.existingOrPlannedBuildingsNear(Protoss_Pylon, 5, position) > 0) return false;

        ProductionOrder order = AddToQueue.withHighPriority(Protoss_Pylon, position);
        return NewConstructionRequest.requestConstructionOf(Protoss_Pylon, position, order);
    }
}
