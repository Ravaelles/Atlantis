package atlantis.production.constructing.position;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.constructing.NewConstructionRequest;
import atlantis.production.constructing.position.conditions.CanPhysicallyBuildHere;
import atlantis.production.constructing.position.protoss.ProtossForbiddenByStreetGrid;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.workers.FreeWorkers;

public class RequestBuildingNear {
    public static String lastError = null;

    private AUnitType type;
    private HasPosition near = null;
    private int maxDistance = MaxBuildingDist.MAX_DIST;
    private boolean specialGridExclusionPermission = false;
    private int maxOtherUnfinishedNearby = 1;

    // =========================================================

    private RequestBuildingNear(AUnitType type) {
        this.type = type;
    }

    public static RequestBuildingNear constructionOf(AUnitType type) {
        return new RequestBuildingNear(type);
    }

    // =========================================================

    public RequestBuildingNear near(HasPosition near) {
        this.near = near;

        return this;
    }

    public RequestBuildingNear maxDistance(int maxDistance) {
        this.maxDistance = maxDistance;

        return this;
    }

    public RequestBuildingNear maxOtherUnfinishedNearby(int maxOtherUnfinishedNearby) {
        this.maxOtherUnfinishedNearby = maxOtherUnfinishedNearby;

        return this;
    }

    public RequestBuildingNear specialGridExclusionPermission() {
        this.specialGridExclusionPermission = true;

        return this;
    }

    // =========================================================

    public ProductionOrder request() {
        if (tooManyNear(type, near)) return error("RequestBuildingNear - Too many " + type + " near " + near);

        if (specialGridExclusionPermission) ProtossForbiddenByStreetGrid.addSpecialGridExclusionPermission(type);

        APosition exact = findExactPosition();
        if (exact == null) return error("RequestBuildingNear - No exact position for " + type + " near " + near);
        if (!exact.isBuildable()) return error("RequestBuildingNear - Not buildable position for " + type);

        ProductionOrder order = AddToQueue.withTopPriority(type, exact);
        if (order == null) return error("RequestBuildingNear - Failed to add " + type + " at " + exact);
        order.setAroundPosition(exact);
        order.markAsUsingExactPosition();
        order.setMinSupply(0);

        Construction construction = NewConstructionRequest.requestConstructionOf(type, exact, order);
        if (construction == null) return error("RequestBuildingNear - Empty construction of " + type + " at " + exact);

        System.err.println("RequestBuilding OK - " + type + " at " + exact);

        lastError = null;
        return order;
    }

    // =========================================================

    private APosition findExactPosition() {
        AUnit builder = FreeWorkers.get().nearestTo(near);

        return FindPosition.findForBuilding(
            builder,
            type,
            null,
            near,
            maxDistance
        );
    }

    private boolean tooManyNear(AUnitType type, HasPosition near) {
        return ConstructionRequests.countNotFinishedOfTypeInRadius(type, maxDistance, near) > maxOtherUnfinishedNearby;
    }

    private ProductionOrder error(String error) {
        lastError = error;
        return null;
    }
}
