package atlantis.production.constructions.position;

import atlantis.game.A;
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
import atlantis.units.select.Select;
import atlantis.units.workers.FreeWorkers;
import atlantis.util.log.ErrorLog;

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

        // The sited door bypasses the spiral checklist, so it must ask
        // the same overlap questions itself: a tile an unfinished or a
        // standing same-type building covers dies downstream (refresh
        // cancel / proximity finish) and the caller re-fires blindly
        // (measured OpenBW: sited PhotonC [115,15] 21x). Refuse here,
        // like the tooManyNear guard above - no order, no construction,
        // no BUILD row for a doomed site.
        if (isSiteBlockedByKnownBuilding(exact, type))
            return error("RequestBuildingNear - Site overlaps a known building for " + type);

        ProductionOrder order = AddToQueue.withTopPriority(type, exact);
        if (order == null) return error("RequestBuildingNear - Failed to add " + type + " at " + exact);
        order.setAroundPosition(exact);
        order.markAsUsingExactPosition();
        order.setMinSupply(0);
        order.setPriority(priority);

        Construction construction = NewConstructionRequest.requestConstructionOf(type, exact, order);
        if (construction == null) return error("RequestBuildingNear - Empty construction of " + type + " at " + exact);

//        System.err.println("RequestBuilding OK - " + type + " at " + exact);

        // Phase A (__PLAN-DECISIONS): sited requests (cannons, forward
        // pylons) bypass the queue door - they land here, so the audit row
        // lands here too, with the picked tile in the reason and the order
        // id as the join key (same oid groups one demand's retries).
        atlantis.decisions.board.DecisionLog.record(
            atlantis.decisions.board.ADecision.decide(
                atlantis.decisions.board.ADecision.Kind.BUILD, type.name(),
                "sited:[" + exact.tx() + "," + exact.ty() + "]",
                atlantis.decisions.board.ADecision.Source.DYNAMIC)
                .frame(A.now())
                .supply(atlantis.game.A.supplyUsed(), atlantis.game.A.supplyTotal())
                .bank(atlantis.game.A.minerals(), atlantis.game.A.gas())
                .orderId(order.id())
                .build());

        lastError = null;
        return order;
    }

    // =========================================================

    private APosition findExactPosition() {
        AUnit builder = FreeWorkers.get().nearestTo(near);

        // Sited demand reaches the catalogue first (same flag, same
        // null-means-legacy contract as the queue funnel): coverage-ranked
        // tiles for cannons, block layouts for the rest. The validations,
        // queueing and construction below stay exactly as they were.
        APosition site = CatalogueSiteAdapter.siteForSited(builder, type, near, maxDistance);
        if (site != null) return site;

        APosition position = FindPosition.findForBuilding(
            builder,
            type,
            null,
            near,
            maxDistance
        );

        if (preventTooManyOfTheSameTypeInOnePlace(position)) return null;

        return position;
    }

    private boolean preventTooManyOfTheSameTypeInOnePlace(APosition position) {
        if (position == null) return false;

        if (
            type.isCannon()
                && Select.ourWithUnfinished(AUnitType.Protoss_Photon_Cannon).countInRadius(8, position) >= 6
        ) {
//            ErrorLog.printMaxOncePerMinute(A.minSec() + " - Prevent excessive cannons at " + position);
            return true;
        }

        return false;
    }

    private boolean tooManyNear(AUnitType type, HasPosition near) {
        return ConstructionRequests.countNotFinishedOfTypeInRadius(type, maxDistance, near) > maxOtherUnfinishedNearby;
    }

    private ProductionOrder error(String error) {
        lastError = error;
        return null;
    }

    /**
     * The sited door bypasses the spiral checklist
     * ({@code PositionFulfillsAllConditions}), so it asks the two overlap
     * questions itself. A tile an unfinished or a standing same-type
     * building covers dies downstream and the caller re-fires blindly -
     * refuse it here, like the tooManyNear guard above. Public as a seam:
     * the pick itself needs a game, the overlap question pins in
     * milliseconds.
     */
    public static boolean isSiteBlockedByKnownBuilding(APosition site, AUnitType type) {
        return atlantis.production.constructions.position.conditions.OverlapsOurUnfinishedBuilding
            .check(site, type)
            || atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding
                .check(site, type);
    }
}
