package atlantis.production.constructions.position;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.NewConstructionRequest;
import atlantis.production.constructions.position.protoss.ProtossForbiddenByStreetGrid;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
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
    private ProductionOrderPriority priority;

    // =========================================================

    private RequestBuildingNear(AUnitType type, ProductionOrderPriority priority) {
        this.type = type;
        this.priority = priority;
    }

    public static RequestBuildingNear constructionOf(AUnitType type) {
        return new RequestBuildingNear(type, ProductionOrderPriority.STANDARD);
    }

    public static RequestBuildingNear constructionOf(AUnitType type, ProductionOrderPriority priority) {
        return new RequestBuildingNear(type, priority);
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
        if (!exact.isBuildableIncludeBuildings())
            return error("RequestBuildingNear - Not buildable position for " + type);

        ProductionOrder order = AddToQueue.withTopPriority(type, exact);
        if (order == null) return error("RequestBuildingNear - Failed to add " + type + " at " + exact);
        order.setAroundPosition(exact);
        order.markAsUsingExactPosition();
        order.setMinSupply(0);
        order.setPriority(priority);

        Construction construction = NewConstructionRequest.requestConstructionOf(type, exact, order);
        if (construction == null) return error("RequestBuildingNear - Empty construction of " + type + " at " + exact);

//        System.err.println("RequestBuilding OK - " + type + " at " + exact);

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
