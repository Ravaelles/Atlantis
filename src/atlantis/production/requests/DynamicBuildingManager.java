package atlantis.production.requests;

import atlantis.map.position.HasPosition;
import atlantis.production.orders.build.AddToQueue;
import atlantis.units.AUnitType;

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

    public HasPosition nextBuildingPosition() {
        return null;
    }

}
