package atlantis.production.requests;

import atlantis.map.position.HasPosition;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.combat.micro.terran.bunker.position.NewBunkerPositionFinder;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.requests.protoss.ProtossPhotonCannonAntiLand;
import atlantis.production.requests.zerg.ZergSunkenColony;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public abstract class DynamicBuildingCommander {

    public abstract AUnitType type();

    public abstract boolean shouldBuildNew();

    public boolean requestToBuildNewAntiAirCombatBuilding() {
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
        return (new NewBunkerPositionFinder(nearTo)).find();
    }

    public HasPosition nextPosition() {
        if (We.terran()) return (new NewBunkerPositionFinder(null)).find();
        if (We.protoss()) return (new ProtossPhotonCannonAntiLand()).nextPosition();
        if (We.zerg()) return (new ZergSunkenColony()).nextPosition();
        return null;
    }

    public int existingWithUnfinished() {
        return Count.ourWithUnfinished(type()) + ConstructionRequests.countNotStartedOfType(type());
    }

}
