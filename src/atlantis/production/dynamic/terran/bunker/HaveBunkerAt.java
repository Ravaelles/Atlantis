package atlantis.production.dynamic.terran.bunker;

import atlantis.architecture.Commander;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public abstract class HaveBunkerAt extends Commander {
    protected boolean bunkerExistsAtPosition() {
        return Count.existingOrPlannedBuildingsNear(AUnitType.Terran_Bunker, 8, atPosition()) > 0;
    }

    protected abstract HasPosition atPosition();

    @Override
    protected boolean handle() {
        return requestBunkerAt(atPosition());

//        if ((new ReinforceWithBunkerAtNearestChoke(atPosition())).invokedCommander()) {
//            return true;
//        }
    }

    protected boolean requestBunkerAt(HasPosition position) {
        return AddToQueue.withHighPriority(Terran_Bunker, position) != null;
    }
}
