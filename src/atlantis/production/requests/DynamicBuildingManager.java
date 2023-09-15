package atlantis.production.requests;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public abstract class DynamicBuildingManager {

    public abstract AUnitType type();

    public abstract boolean shouldBuildNew();

    public boolean handleBuildNew() {
        if (shouldBuildNew()) {
            return requestOne(nextBuildingPosition());
        }

        return false;
    }

    public boolean requestOne(HasPosition at) {
        AddToQueue.withStandardPriority(type(), at);
        return true;
    }

    /**
     * Different for Zerg e.g. Creep Colony for Sunken Colony.
     */
    public AUnitType typeToBuildFirst() {
        return type();
    }

    /**
     * null will be changed later to become something like "in main".
     */
    public HasPosition nextBuildingPosition() {
        return null;
    }

    public int existingWithUnfinished() {
        return Count.ourOfTypeWithUnfinished(type()) + ConstructionRequests.countNotStartedOfType(type());
    }

}
