package atlantis.production.constructing.builders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.conditions.CanPhysicallyBuildHere;
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
        AUnitType buildingType = construction.buildingType();

        if (unit == null) {
            throw new RuntimeException("unit empty");
        }
        if (buildPosition == null) {
            System.err.println("buildPosition is null (travelToConstruct " + buildingType + ")");
            construction.cancel();
            return false;
        }

        // =========================================================

        double minDistanceToIssueBuildOrder = minDistanceToIssueBuildOrder(buildingType);

        double distance = unit.distTo(buildPositionCenter);
        String distString = "(" + A.digit(distance) + ")";

//        System.err.println(A.now() + " distance = " + distString + " / minerals=" + A.minerals());

        if (notEnoughMineralsYet(distance, buildingType)) return false;

//        CameraCommander.centerCameraOn(unit.getPosition());

        if (distance > minDistanceToIssueBuildOrder) {
//            if (buildingType.isBase()) System.err.println("MoveToConstruct " + distance);
//            System.err.println(A.now() + " MOVE TO CONS = " + distString + " / minerals=" + A.minerals());
            return moveToConstruct(construction, buildingType, distance, distString);
        }
        else {
//            if (
//                ((A.everyNthGameFrame(77) || unit.hasNotMovedInAWhile()) && distance <= 2.1)
//                    || !CanPhysicallyBuildHere.check(unit, buildingType, buildPosition)
//            ) {
            if (shouldRefreshConstructionPosition(buildingType, buildPosition)) {
//                System.err.println(A.now() + " Refresh " + buildingType + " position");
                refreshConstructionPositionIfNeeded(construction, buildingType);
                return false;
            }

            return issueBuildOrder(construction);
        }
    }

    private boolean shouldRefreshConstructionPosition(AUnitType buildingType, APosition buildPosition) {
        return A.everyNthGameFrame(47)
            && buildPosition.isPositionVisible()
            && !CanPhysicallyBuildHere.check(unit, buildingType, buildPosition);
    }

    private boolean notEnoughMineralsYet(double distance, AUnitType buildingType) {
        if (
            distance <= 12
                && !A.canAfford(buildingType.mineralPrice() - 35, buildingType.gasPrice() - 20)
        ) return true;

        return false;
    }

    private static double minDistanceToIssueBuildOrder(AUnitType buildingType) {
        double minDistanceToIssueBuildOrder = 2.1;

        if (buildingType.isBunker()) minDistanceToIssueBuildOrder = 8;
        else if (buildingType.isGasBuilding()) minDistanceToIssueBuildOrder = 3.5;

        return minDistanceToIssueBuildOrder;
    }

    public static APosition refreshConstructionPositionIfNeeded(Construction construction, AUnitType buildingType) {
        if (
            buildingType.isGasBuilding() || buildingType.isBase()
        ) return construction.buildPosition();

        if (shouldRefreshConstructionPosition(construction, buildingType)) {
            APosition positionForNewBuilding = construction.findPositionForNewBuilding();

            if (positionForNewBuilding != null) {
                construction.setPositionToBuild(positionForNewBuilding);
                Construction.clearCache();
            }
        }

        return construction.buildPosition();
    }

    private static boolean shouldRefreshConstructionPosition(Construction construction, AUnitType buildingType) {
        return !CanPhysicallyBuildHere.check(
            construction.builder(), buildingType, construction.buildPosition()
        );
//                ||
//                (construction.builder().looksIdle() && AGame.everyNthGameFrame(151));
    }

    private boolean moveToConstruct(Construction construction, AUnitType buildingType, double distance, String distString) {
        if (shouldNotTravelYet(buildingType, distance)) {
            unit.setTooltipTactical("Wait to build " + buildingType.name() + distString);
            return false;
        }

        if (!unit.isMoving()) {
//                if (A.everyNthGameFrame(20)) {
//                    construction.setPositionToBuild(newPosition);
//                }

//                    GameSpeed.changeSpeedTo(60);
            if (unit.move(
                construction.positionToBuildCenter(),
                Actions.MOVE_BUILD,
                "Build " + buildingType.name() + distString,
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
            System.err.println("builder.productionOrder() = " + builder.construction().productionOrder());
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

        if (AGame.canAfford(buildingType.mineralPrice(), buildingType.gasPrice())) {
//            System.err.println("buildPosition PRE = " + construction.buildPosition());
//            APosition buildPosition = refreshBuildPosition(construction);
//            APosition buildPosition = refreshConstructionPositionIfNeeded(construction, buildingType);
            APosition buildPosition = construction.buildPosition();

            if (buildPosition == null) return false;

//            System.err.println("buildPosition POST = " + buildPosition);
//            System.err.println("buildPosition.translateByTiles(1, 1) = " + buildPosition.translateByTiles(1, 1));

            moveOtherUnitsOutOfConstructionPlace(buildPosition.translateByTiles(-3, -3));

            // If place is ok, unit isn't constructing, and we can afford it, issue the build command.
            buildPosition = (new GasBuildingFix(unit)).applyGasBuildingFixIfNeeded(buildPosition, buildingType);

            if (buildPosition == null) {
                construction.cancel();
                ErrorLog.printMaxOncePerMinute("Cancel construction of " + buildingType + " because position null");
                return false;
            }

            TilePosition buildTilePosition = new TilePosition(
                buildPosition.tx(), buildPosition.ty()
            );

//            if (Select.ourWithUnfinishedOfType(AtlantisRaceConfig.GAS_BUILDING).inRadius(3, buildPosition).notEmpty()) {
//                construction.cancel();
//                return false;
//            }

            if (!unit.isConstructing() || unit.isIdle() || AGame.now() % 5 == 0) {
//                A.println("Building " + buildingType + " at " + buildTilePosition + ", construction: " + construction);
                unit.build(buildingType, buildTilePosition);
                return true;
            }
        }

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
////        return order.buildPosition();
//    }

    private void moveOtherUnitsOutOfConstructionPlace(APosition buildPosition) {
        for (AUnit unit : unit.friendsNear().groundUnits().inRadius(2.3, buildPosition).exclude(unit).list()) {
            unit.moveAwayFrom(buildPosition, 1, Actions.SPECIAL, "Construction!");
        }
    }

    // =========================================================

    private boolean shouldNotTravelYet(AUnitType building, double distance) {
        if (We.zerg()) return false;

//        if (AGame.timeSeconds() < 300 && !building.isBase()) {
        if (AGame.timeSeconds() < 300) {
            int baseBonus = building.isBase() ? 80 : 0;
//            return !AGame.canAfford(

//            if (building.is(AUnitType.Zerg_Spawning_Pool)) {

//                    "mineralsRes=" + ReservedResources.minerals()
//                    + ", queuePos=" + ProductionQueue.positionInQueue(building)
//                );
//            }

//            if (ProductionQueue.isAtTheTopOfQueue(building, 1)) {
            return !AGame.canAfford(
                building.mineralPrice() - 32 - (int) (distance * 1.3) - baseBonus,
                building.gasPrice() - 16 - (int) distance
            );
//            }
//
//            return !AGame.canAffordWithReserved(
//                     building.getMineralPrice() - 32 - (int) (distance * 1.3),
//                    building.getGasPrice() - 16 - (int) distance
//            );
        }

        return false;
    }

}
