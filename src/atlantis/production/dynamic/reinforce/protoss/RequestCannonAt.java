package atlantis.production.dynamic.reinforce.protoss;

import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.CountInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;
import static atlantis.units.AUnitType.Protoss_Pylon;

public class RequestCannonAt {
    public static boolean at(HasPosition position) {
        if (Select.ourOfType(Protoss_Pylon).inRadius(6, position).count() == 0) {
            buildPylonFirst(position);
            return true;
        }

        return requestCannon(position);
    }

    private static boolean requestCannon(HasPosition position) {
        AddToQueue.withHighPriority(Protoss_Photon_Cannon, position);
        return false;
    }

    private static void buildPylonFirst(HasPosition position) {
        if (Count.inQueueOrUnfinished(Protoss_Pylon, 99) >= 3) return;
        if (Count.existingOrPlannedBuildingsNear(Protoss_Pylon, 5, position) > 0) return;

        AddToQueue.withHighPriority(Protoss_Pylon, position);
    }
}
