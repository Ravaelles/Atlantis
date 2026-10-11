package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.conditions.can_build_here.CanPhysicallyBuildHere;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.add.AddToQueue;
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

//        if (construction.buildingType().isCyberneticsCore()) {
//            System.err.println(A.minSec() + " Travel? CyberneticsCore");
//        }

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
        double distanceToConstruction = unit.groundDist(buildPositionCenter);

        MoveUnitsFromConstructionPlace.move(unit, construction, distanceToConstruction);

        if (shouldMoveToConstruct(construction, distanceToConstruction, minDistanceToIssueBuildOrder)) {
            return moveToConstruct(construction, type, distanceToConstruction);
        }

        else {
//            if (type.isCyberneticsCore()) System.err.println(A.minSec() + " Dont MOVE TO CyberneticsCore");

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

//        if (type.isCyberneticsCore()) System.err.println(A.minSec() + " Travel? CyberneticsCore");

        if (ShouldNotTravelToConstructYet.check(type, distance)) {
//            if (type.isCyberneticsCore()) System.err.println(A.minSec() + "     ########## NO !!!");
            construction.builder().setTooltipTactical("Wait to build " + type.name() + "(" + A.digit(distance) + ")");
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
//        return (!type.isGateway() ? 0 : ReservedResources.minerals())

        Orders notStartedEarlierOrders = Queue.get()
            .notStarted()
            .supplyAtMost(order.minSupply());

        int penaltyIfManyOtherOrders = 0;
        if (notStartedEarlierOrders.size() > 0) {
//            notStartedEarlierOrders.print("Not started earlier orders that " + type);

            if (notStartedEarlierOrders.size() >= 2) penaltyIfManyOtherOrders = 130;
            else if (notStartedEarlierOrders.size() >= 1) penaltyIfManyOtherOrders = 40;
        }

        int needMinerals = (!type.isGateway() ? 0 : 140)
            + type.mineralPrice()
            + penaltyIfManyOtherOrders
            + (A.supplyUsed(9) ? -150 : -96); // Quicker-bonus when have more workers

//        if (type.isGateway()) {
//            System.err.println("needMinerals = " + needMinerals + " / " + type + " / penalty:" + penaltyIfManyOtherOrders);
//        }

        return needMinerals;
    }

    private static double minDistanceToIssueBuildOrder(AUnitType buildingType) {
        double minDistanceToIssueBuildOrder = 1.4;

        if (buildingType.isBunker()) minDistanceToIssueBuildOrder = 8;
        else if (buildingType.isGasBuilding()) minDistanceToIssueBuildOrder = 3.5;

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

    private static boolean builderHasNotMovedInAWhile(Construction construction) {
        return construction.builder().lastPositionChangedMoreThanAgo(90)
            && construction.builder().distTo(construction.buildPosition()) <= 5;
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

            if (unit.move(
                buildCenter,
                Actions.MOVE_BUILD,
                "Build " + buildingType.name() + A.digit(dist),
                true
            )) return true;
        }

        return true;
    }
}
