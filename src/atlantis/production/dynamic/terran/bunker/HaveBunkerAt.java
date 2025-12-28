package atlantis.production.dynamic.terran.bunker;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

import static atlantis.units.AUnitType.Terran_Bunker;

public abstract class HaveBunkerAt extends Commander {
    protected boolean bunkerExistsAtPosition() {
        if (Count.existingOrPlannedBuildingsNear(AUnitType.Terran_Bunker, 8, atPosition()) > 0) return true;

        return false;
    }

    protected abstract HasPosition atPosition();

    @Override
    protected boolean handle() {
        if (requestBunkerAt(atPosition())) {
            return true;
        }

//        if ((new ReinforceWithBunkerAtNearestChoke(atPosition())).invokedCommander()) {
//            return true;
//        }

        return false;
    }

    protected boolean requestBunkerAt(HasPosition position) {
        return AddToQueue.withHighPriority(Terran_Bunker, position) != null;
    }
}
