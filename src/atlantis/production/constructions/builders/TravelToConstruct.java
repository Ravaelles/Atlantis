package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.player.Enemy;
import atlantis.information.strategy.Strategy;
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
import bwapi.TilePosition;

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

//        if (waitForMoreMineralsBeforeTravelling(
//            distanceToConstruction, type, construction.productionOrder()
//        )) return false;

        if (shouldMoveToConstruct(construction, distanceToConstruction, minDistanceToIssueBuildOrder)) {
            return moveToConstruct(construction, type, distanceToConstruction);
        }

        else {
//            if (type.isCyberneticsCore()) System.err.println(A.minSec() + " Dont MOVE TO CyberneticsCore");

            if (A.canAfford(type)) {
                buildPosition = handleRefreshingPositionIfNeeded(construction, type, buildPosition);

                if (
                    A.everyNthGameFrame(97)
                        && !CanPhysicallyBuildHere.check(unit, type, buildPosition)
                        && (builder == null || builder.lastPositionChangedMoreThanAgo(30 * 8))
                ) {
                    construction.cancel(type + " Can't build here");

                    A.errPrintln("Can't build here " + type + ", so cancel + re-request");
                    AddToQueue.withTopPriority(
                        type,
                        construction.positionToBuildCenter()
                    );
                    return false;
                }
            }

            return issueBuildOrder(construction);
        }
    }

    private static APosition handleRefreshingPositionIfNeeded(
        Construction construction, AUnitType buildingType, APosition buildPosition
    ) {
        if (shouldRefreshConstructionPosition(construction)) {
            AbstractPositionFinder.clearCache();

            System.err.println(A.minSec() + " Refresh " + buildingType + " position");
            AbstractPositionFinder.clearCache();

            buildPosition = refreshConstructionPositionIfNeeded(construction);
        }

        if (shouldRefreshConstructionPosition(construction)) {
            System.err.println(A.minSec() + " WTF?!? AGAIN Refresh " + buildingType + " position");
            buildPosition = refreshConstructionPositionIfNeeded(construction);
        }

        return buildPosition;
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

    private boolean asProtossMultiBuilderDoNotSwitchConstructions(AUnit builder) {
        if (!We.protoss()) return false;

        if (builder.lastActionLessThanAgo(20, Actions.MOVE_BUILD)) return true;

        return false;
    }

    private static boolean shouldRefreshConstructionPosition(Construction construction) {
        AUnit builder = construction.builder();
        AUnitType buildingType = construction.buildingType();
        APosition buildPosition = construction.buildPosition();

        if (buildPosition == null) {
            System.err.println("buildPosition IS NULL - refresh " + buildingType);
            return true;
        }
        if (!buildPosition.isBuildableIncludeBuildings() && !construction.buildingType().isGasBuilding()) {
            System.err.println("buildPosition NOT BUILDABLE - refresh " + buildingType);
            return true;
        }

        return A.everyNthGameFrame(23)
            && buildPosition.isPositionVisible()
            && !CanPhysicallyBuildHere.check(builder, buildingType, buildPosition);
    }

//    private boolean waitForMoreMineralsBeforeTravelling(double distance, AUnitType type, ProductionOrder order) {
//        if (!unit.isGatheringMinerals()) return false;
//
//        if (distance <= 15) {
//            if (
//                !A.canAfford(
////                    ReservedResources.minerals() - 32,
////                    ReservedResources.gas() - 16
//                    ReservedResources.minerals() - type.mineralPrice() - 32,
//                    ReservedResources.gas() - type.gasPrice() - 16
////                                    type.mineralPrice() + ReservedResources.minerals() - 32,
////                                    type.gasPrice() + ReservedResources.gas() - 16
//                )
//            ) {
////                System.out.println(A.minSec() + " wait for more MINERALS - " + type);
//                return true;
//            }
//        }
//
//        // Farther than 15 tiles from base
//        else {
//            if (!A.hasMinerals(needThisMineralsForLongDistanceConstructionTravel(distance, type, order))) {

    /// /                System.out.println(A.minSec() + " !!!! WAIT FOR MORE MINERALS - " + type);
//                return true;
//            }
//        }
//
//        return false;
//    }
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

    public static APosition refreshConstructionPositionIfNeeded(Construction construction) {
        AUnitType buildingType = construction.buildingType();

        if (
            buildingType.isGasBuilding() || (buildingType.isBase() && !Enemy.terran() && !Enemy.zerg())
        ) return construction.buildPosition();

        if (shouldRefreshConstructionPosition(construction)) {
            refreshPosition(construction);
        }

//        return construction.positionToBuildCenter();
        return construction.buildPosition();
    }

    private static APosition refreshPosition(Construction construction) {
        if (doNotRefreshPosition(construction)) return construction.buildPosition();

        Construction.clearCache();
        AbstractPositionFinder.clearCache();

        APosition positionForNewBuilding = construction.findPositionForNewBuilding();
        if (positionForNewBuilding != null) {
            construction.setPositionToBuild(positionForNewBuilding);
            Construction.clearCache();
        }

        return construction.buildPosition();
    }

    private static boolean doNotRefreshPosition(Construction construction) {
        return construction.buildingType().isGasBuilding();
    }

    private static boolean cantBuildHere(Construction construction, AUnitType buildingType) {
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
            APosition buildCenter = construction.positionToBuildCenter();
            if (!buildCenter.isWalkable()) {
                ErrorLog.printMaxOncePerMinute(A.minSec() + " ##### Unwalkable buildCenter for " + buildingType);

                refreshPosition(construction);

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

    private boolean issueBuildOrder(Construction construction) {
        AUnitType buildingType = construction.buildingType();

        AUnit builder = construction.builder();
        if (builder != null && builder.buildUnit() != null) {
            System.err.println("builder = " + builder);
            System.err.println("builder.buildUnit() = " + builder.buildUnit());
            System.err.println("builder.construction() = " + builder.construction());
            if (builder.construction() != null) {
                System.err.println("builder.productionOrder() = " + builder.construction().productionOrder());
            }
        }

//        if (We.protoss()) {
//            AUnit newBuilding = Select.ourUnfinished()
//                .ofType(buildingType)
//                .inRadius(2, unit).nearestTo(unit);
//            if (newBuilding != null) {
//                construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
//                construction.setBuilder(null);
//                unit.stop("Finished!");
//                return false;
//            }
//        }

        if (A.canAfford(buildingType.mineralPrice(), buildingType.gasPrice())) {
//            System.err.println("buildPosition PRE = " + construction.buildPosition());
//            APosition buildPosition = refreshBuildPosition(construction);
//            APosition buildPosition = refreshConstructionPositionIfNeeded(construction, buildingType);
            APosition buildPosition = construction.buildPosition();

            if (buildPosition == null) {
                ErrorLog.printMaxOncePerMinute("Cancel constr of " + buildingType + " because position null");
                return false;
            }

//            System.err.println("buildPosition POST = " + buildPosition);
//            System.err.println("buildPosition.translateByTiles(1, 1) = " + buildPosition.translateByTiles(1, 1));

            moveOtherUnitsOutOfConstructionPlace(buildPosition.translateByTiles(-3, -3));

            // If place is ok, unit isn't constructing, and we can afford it, issue the build command.
            buildPosition = (new GasBuildingFix(unit)).applyGasBuildingFixIfNeeded(buildPosition, buildingType);

            if (buildPosition == null) {
                if (construction.buildingUnit() == null) {
                    construction.cancel(buildingType + " has null buildPosition");
                    ErrorLog.printMaxOncePerMinute("Cancel construction of " + buildingType + " because position null");
                }

                construction.setBuilder(null);

                return false;
            }

//            if (Select.ourWithUnfinishedOfType(AtlantisRaceConfig.GAS_BUILDING).inRadius(3, buildPosition).notEmpty()) {
//                construction.cancel();
//                return false;
//            }

            if (!unit.isConstructing() || AGame.now() % 37 == 0) {
//                A.println("_CONSTRUCT_ " + buildingType + " at " + buildTilePosition + ", construction: " + construction);

                if (Strategy.get().isExpansion() && A.supplyUsed() <= 17) {
                    refreshConstructionPositionIfNeeded(construction);
                }

                if (unit.lastActionMoreThanAgo(20)) {
                    TilePosition buildTilePosition = new TilePosition(buildPosition.tx(), buildPosition.ty());

                    if (cantBuildHere(construction, buildingType)) {
                        refreshPosition(construction);
                        ErrorLog.printMaxOncePerMinute(
                            "Couldn't build " + buildingType + " at " + buildPosition
                                + ". Refreshed to " + construction.buildPosition()
                        );
                        buildTilePosition = new TilePosition(buildPosition.tx(), buildPosition.ty());
                    }

                    if (unit.build(buildingType, buildTilePosition, construction)) {
                        return true;
                    }

                    if (
                        A.canAfford(buildingType.mineralPrice() - 8, buildingType.gasPrice())
                            && cantBuildHere(construction, buildingType)
                    ) {
                        AbstractPositionFinder.clearCache();

                        refreshPosition(construction);
                        ErrorLog.printMaxOncePerMinute(
                            "Couldn't build " + buildingType + " so force-refreshed to " + construction.buildPosition()
                        );
                    }

                    if (unit.build(buildingType, buildTilePosition, construction)) {
                        return true;
                    }
//                    else {
//                        if (unit.lastPositionChangedMoreThanAgo(30 * 2)) construction.cancel();
//                    }
                }
//                System.err.println("unit.A = " + unit.action().name());
//                System.err.println("unit.B = " + unit.getLastCommandRaw().getType().name());
                return true;
            }
        }

        unit.setTooltip("CantAfford" + buildingType.name() + "Yet");
        return true;
    }

//    private APosition refreshBuildPosition(Construction order) {
//        if (order.buildingType().isGasBuilding()) return order.buildPosition();
//
////        if (Select.ourWorkers().inRadius(1.8, order.buildPosition()).atLeast(2)) {
//        return APositionFinder.findStandardPosition(
//            order.builder(), order.buildingType(), order.buildPosition(), 15
//        );
////            return APositionFinder.findPositionForNew(
////                order.unit(), order.buildingType(), order
////            );
////        }
//

    /// /        return order.buildPosition();
//    }
    private void moveOtherUnitsOutOfConstructionPlace(APosition buildPosition) {
        for (AUnit unit : unit.friendsNear().groundUnits().inRadius(2.3, buildPosition).exclude(unit).list()) {
            unit.moveAwayFrom(buildPosition, 1, Actions.SPECIAL, "Construction!");
        }
    }
}
