package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.Orders;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;

public class TravelToConstruct extends HasUnit {
    public TravelToConstruct(AUnit unit) {
        super(unit);
    }

    protected boolean travelWhenReady(Construction construction) {
        APosition buildPosition = construction.buildPosition();
        APosition buildPositionCenter = construction.positionToBuildCenter();
        AUnitType type = construction.buildingType();
        AUnit builder = construction.builder();

        // A builderless construction is the retry path's business (it
        // unlinks and re-queues): traveling nowhere with no one must not
        // throw here. Previously a null builder NPE'd in the transit
        // check below.
        if (builder == null) return false;

        if (unit == null) {
            throw new RuntimeException("unit empty");
        }
        if (buildPosition == null) {
            System.err.println("buildPosition is null (travelToConstruct " + type + ")");
            construction.cancel(type + " buildPosition is null");
            return false;
        }

        if (asProtossMultiBuilderDoNotSwitchConstructions(builder)) return false;

        // =========================================================

        double minDistanceToIssueBuildOrder = minDistanceToIssueBuildOrder(type);

        // THE distance is measured to the footprint CENTER - the point the
        // worker below walks to. Decision and feet must read the same
        // geometry: a target the metric never reaches walks forever (Terran
        // Barracks stall, NEXT #56: walking to the 4x3 center while measuring
        // to its origin tile kept ~2.5 > 1.4 forever, so the SCV re-moved
        // every 13th frame at distToSite=1.4 with zero attempts). The older
        // tile-origin metric had the twin defect in reverse (NEXT #59: the
        // center can never be within a 1.4-tile threshold) - both are fixed
        // together by measuring to the walk target with a footprint-sized
        // threshold below. The engine walks the worker the last ~70px itself
        // and seats the builder, so issuing from the threshold edge is safe.
        double distanceToConstruction = distanceForBuildOrderIssue(unit, construction, type);

        MoveUnitsFromConstructionPlace.move(unit, construction, unit.groundDist(buildPositionCenter));

        if (shouldMoveToConstruct(construction, distanceToConstruction, minDistanceToIssueBuildOrder)) {
            return moveToConstruct(construction, type, distanceToConstruction);
        }

        else {

            if (RefreshConstructionPosition.finalRefreshBeforeIssuingOrderFailed(
                unit, construction, type, buildPosition, builder)
            ) return false;

            return (new IssueBuildOrder(unit)).considerIssuingNow(construction);
        }
    }

    private static boolean shouldMoveToConstruct(
        Construction construction, double distance, double minDistanceToIssueBuildOrder
    ) {
        if (distance <= minDistanceToIssueBuildOrder) return false;

        AUnitType type = construction.buildingType();


        if (ShouldNotTravelToConstructYet.check(construction, construction.builder(), distance)) {
            if (construction.builder() != null) {
                construction.builder().setTooltipTactical("Wait to build " + type.name() + "(" + A.digit(distance) + ")");
            }
            return false;
        }

        return true;
    }

    /**
     * "Do not switch this Protoss builder to another construction while it is still
     * moving" - which is a rule about a worker IN TRANSIT, and it was being applied to a
     * worker that had ARRIVED.
     *
     * <p>
     * The check is `lastActionLessThanAgo(20, MOVE_BUILD)`, and the problem is who refreshes
     * that action: `moveToConstruct` re-issues the move every time the unit is not moving
     * (or every 13th frame), so a builder standing at its site with an unfinished walk has a
     * `MOVE_BUILD` action that is ALWAYS younger than 20 frames. The method therefore
     * returned true forever, `travelWhenReady` returned false before reaching
     * `IssueBuildOrder`, and the build order was never issued. Measured (NEXT.md #53): a
     * builder one tile from its Gateway with `lastCommand=Move`, `action=MOVE_BUILD` and
     * `lastActionAgo` climbing 0..44 frame after frame, never `Build`.
     * </p>
     *
     * <p>
     * The rule's own intent survives in the guard added here: a builder is only "still
     * travelling" if it is actually some distance from the site. Once it is close enough to
     * be issuing the order, holding it back protects nothing - it is the state the order is
     * waiting for.
     * </p>
     */
    private boolean asProtossMultiBuilderDoNotSwitchConstructions(AUnit builder) {
        if (!We.protoss()) return false;

        // Arrived (or close enough to build): never block the order on a rule about transit.
        Construction construction = builder.construction();
        if (construction != null && construction.buildPosition() != null
            && builder.distTo(construction.buildPosition()) <= 2.5) {
            return false;
        }

        return builder.lastActionLessThanAgo(20, Actions.MOVE_BUILD);
    }

    public int needThisMineralsForLongDistanceConstructionTravel(double distance, AUnitType type, ProductionOrder order) {

        Orders notStartedEarlierOrders = Queue.get()
            .notStarted()
            .supplyAtMost(order.minSupply());

        int penaltyIfManyOtherOrders = 0;
        if (notStartedEarlierOrders.size() > 0) {

            if (notStartedEarlierOrders.size() >= 2) penaltyIfManyOtherOrders = 130;
            else if (notStartedEarlierOrders.size() >= 1) penaltyIfManyOtherOrders = 40;
        }

        int needMinerals = (!type.isGateway() ? 0 : 140)
            + type.mineralPrice()
            + penaltyIfManyOtherOrders
            + (A.supplyUsed(9) ? -150 : -96); // Quicker-bonus when have more workers


        return needMinerals;
    }

    /**
     * The distance the issue-threshold is compared against: the footprint
     * center for every type (NEXT #70 established it for gas; the owner call
     * extends it after the #56 hover). The worker walks to the center, so a
     * tile-origin metric parks 4-wide footprints past any fixed line.
     * Package-visible as a seam so the metric choice is assertable.
     */
    public static double distanceForBuildOrderIssue(AUnit unit, Construction construction, AUnitType type) {
        if (construction.positionToBuildCenter() != null) {
            return unit.distTo(construction.positionToBuildCenter());
        }
        return unit.distTo(construction.buildPosition());
    }

    private static double minDistanceToIssueBuildOrder(AUnitType buildingType) {
        double minDistanceToIssueBuildOrder = 1.4;

        if (buildingType.isBunker()) minDistanceToIssueBuildOrder = 8;
        else if (buildingType.isGasBuilding()) minDistanceToIssueBuildOrder = 3.5;
        else {
            // The threshold must cover the walk target: a 4x3 center sits
            // ~2.5 tiles from its own origin, so a flat 1.4 would re-create
            // the #56 hover with the metric and target now agreeing. Small
            // footprints keep 1.4 (Pylon 2x2: ~1.41 - the working path).
            double halfWidth = buildingType.getTilesWidth() / 2.0;
            double halfHeight = buildingType.getTilesHeights() / 2.0;
            minDistanceToIssueBuildOrder = Math.max(
                1.4, Math.sqrt(halfWidth * halfWidth + halfHeight * halfHeight));
        }

        return minDistanceToIssueBuildOrder;
    }

    protected static boolean cantBuildHere(Construction construction, AUnitType buildingType) {
        return !CanPhysicallyBuildHere.check(
            construction.builder(), buildingType, construction.buildPosition()
        ) && preventCantBuildHereForProtoss(buildingType);
    }

    private static boolean preventCantBuildHereForProtoss(AUnitType buildingType) {
        if (!We.protoss()) return false;
        return A.supplyTotal() <= 11 && buildingType.isGateway();
    }

    private boolean moveToConstruct(Construction construction, AUnitType buildingType, double dist) {
        if (!unit.isMoving() || A.everyNthGameFrame(13)) {
            RefreshConstructionPosition.refreshIfNeeded(construction);

            APosition buildCenter = construction.positionToBuildCenter();
            if (!buildCenter.isWalkable()) {
                ErrorLog.printMaxOncePerMinute(A.minSec() + " ##### Unwalkable buildCenter for " + buildingType);

                RefreshConstructionPosition.refreshPosition(construction);

                ErrorLog.printMaxOncePerMinutePlusPrintStackTrace(
                    "Can't walk to " + buildCenter + " (" + buildingType + ", buildable: " + buildCenter.isBuildableIncludeBuildings() + "),"
                        + "\n    refresh position to: "
                        + construction.positionToBuildCenter() + " (buildable: " + buildCenter.isBuildableIncludeBuildings() + ")"
                );

                buildCenter = construction.positionToBuildCenter();
            }

            // Walk to the footprint CENTER (owner call: the builder visibly
            // belongs at the middle of the site, not its top-left corner).
            // Consistent with the decision above by construction: both read
            // distTo(center), and the threshold is footprint-sized, so arrival
            // always counts - the hover needed a metric the target could never
            // reach, and that combination is gone.
            if (unit.move(
                buildCenter,
                Actions.MOVE_BUILD,
                "Build " + buildingType.name() + A.digit(dist),
                true
            )) return true;
        }

        // NOT `true` unconditionally. The old `return true` (and the same on the branch above)
        // told the caller "the worker was handled" on every frame - including frames where no
        // move was issued - so `travelWhenReady` never fell through to `IssueBuildOrder` while
        // the distance test above said "still moving". Measured 2026-10-11: a Probe parked on
        // its own Gateway tile for 43s, `MOVE_BUILD` refreshed every 13th frame, no build ever
        // attempted. `true` now means "an order was issued this frame"; false lets the caller
        // try the build.
        return false;
    }
}
