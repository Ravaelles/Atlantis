package atlantis.production.requests;

import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.dynamic.expansion.secure.SecuringWithBunkerPosition;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public abstract class DynamicBuildingCommander {

    public abstract AUnitType type();

    public abstract boolean shouldBuildNew();

    public boolean handleBuildNew() {
        if (shouldBuildNew()) {
            return requestOne(nextPosition());
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

    public HasPosition nextPosition(HasPosition nearTo) {
        return SecuringWithBunkerPosition.forNonNatural(nearTo);
    }

    public HasPosition nextPosition() {
        return SecuringWithBunkerPosition.bunkerPosition();
    }

    public int existingWithUnfinished() {
        return Count.ourOfTypeWithUnfinished(type()) + ConstructionRequests.countNotStartedOfType(type());
    }

}
