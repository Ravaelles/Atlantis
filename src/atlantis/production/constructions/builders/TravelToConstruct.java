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

        // THE distance is a PLAIN distance to the TILE the worker must stand on. Two mistakes
        // were measured here (NEXT.md #59), and both kept `shouldMoveToConstruct` true while the
        // worker was already standing on its site, so `moveToConstruct` ran every frame,
        // `IssueBuildOrder` - the only path that calls `unit.build` - was never reached, and the
        // construction was cancelled at 35-43s as "took too long / buildable:true":
        //
        //  1. the CENTRE of the building instead of its tile. `positionToBuildCenter()` is
        //     offset by the building's tile dimensions, so for a 4x3 Gateway the centre is
        //     ~1.8 tiles from the tile it belongs to - and 1.8 > the 1.4 threshold, forever.
        //  2. `groundDist` instead of `distTo`. `groundDistanceTo` goes through JBWEB
        //     (`PositionUtil`), which `_AI/POSITION-FINDER.md` point 3 already records as
        //     unusable on OpenBW. Measured side by side on the same worker and tile:
        //     `distTo=1.3` but `groundDist=1.8` - the JBWEB-mediated number is inflated, and it
        //     was the one being compared against a 1.4-tile threshold.
        //
        // `distTo` is a plain geometric distance and is the metric the threshold was written
        // for. The worker at `distTo=1.3` is within reach; at 1.8 it was told to keep walking to
        // the tile it was already on.
        //
        // Gas buildings measure to the footprint CENTER, not the tile origin (NEXT #70):
        // a refinery is 4x2, so its center sits ~2.2 tiles from its origin tile and a
        // worker adjacent to the geyser reads ~3.6 from the origin - past the 3.5 gas
        // threshold forever ("took too long (34s)", measured 20261013_100226: worker
        // plateaued at 3.7 with every canBuild gate true, zero attempts). The engine
        // answers reachability itself (`canBuild` was true there); our heuristic must
        // not veto what the engine allows. Other types keep the tile metric - for a
        // 2x2 Pylon the center sits exactly ON the 1.4 boundary and re-metricing it
        // would churn a working path.
        double distanceToConstruction = distanceForBuildOrderIssue(unit, construction, type);

        MoveUnitsFromConstructionPlace.move(unit, construction, unit.groundDist(buildPositionCenter));

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

    /**
     * The distance the issue-threshold is compared against. Plain tile distance
     * for most buildings; footprint-center distance for gas (NEXT #70 - a 4x2
     * refinery's center is ~2.2 tiles from its origin, so tile distance reads
     * ~3.6 for an adjacent worker and never crosses the 3.5 gas threshold).
     * Package-visible as a seam so the metric choice is assertable.
     */
    public static double distanceForBuildOrderIssue(AUnit unit, Construction construction, AUnitType type) {
        if (type.isGasBuilding() && construction.positionToBuildCenter() != null) {
            return unit.distTo(construction.positionToBuildCenter());
        }
        return unit.distTo(construction.buildPosition());
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
