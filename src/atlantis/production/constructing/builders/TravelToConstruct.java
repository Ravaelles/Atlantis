package atlantis.production.constructing.builders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionOrderStatus;
import atlantis.production.constructing.position.conditions.CanPhysicallyBuildHere;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;
import atlantis.util.log.ErrorLog;
import bwapi.TilePosition;

public class TravelToConstruct extends HasUnit {
    public TravelToConstruct(AUnit unit) {
        super(unit);
    }

    protected boolean travel(Construction construction) {
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

        double minDistanceToIssueBuildOrder = buildingType.isGasBuilding() ? 2.6 : 1.1;
        double distance = unit.distTo(buildPositionCenter);
        String distString = "(" + A.digit(distance) + ")";

//        CameraCommander.centerCameraOn(unit.getPosition());

        if (distance > minDistanceToIssueBuildOrder) {
            return moveToConstruct(construction, buildingType, distance, distString);
        }
        else {
            if (A.everyNthGameFrame(67)) {
                refreshConstructionPositionIfNeeded(construction, buildingType);
            }

            return issueBuildOrder(buildingType, construction);
        }
    }

    private static APosition refreshConstructionPositionIfNeeded(Construction construction, AUnitType buildingType) {
        if (
            buildingType.isGasBuilding() || buildingType.isBase()
        ) return construction.buildPosition();

        if (shouldRefreshConstructionPosition(construction, buildingType)) {
            APosition positionForNewBuilding = construction.findPositionForNewBuilding();
//            System.out.println("UPDATED positionForNewBuilding = " + positionForNewBuilding);
            if (positionForNewBuilding != null) {
                construction.setPositionToBuild(positionForNewBuilding);
                Construction.clearCache();
            }
        }

        return construction.buildPosition();
    }

    private static boolean shouldRefreshConstructionPosition(Construction construction, AUnitType buildingType) {
        return !CanPhysicallyBuildHere.check(construction.builder(), buildingType, construction.buildPosition())
            || (construction.builder().looksIdle() && AGame.everyNthGameFrame(31));
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

    private boolean issueBuildOrder(AUnitType buildingType, Construction order) {
        if (We.protoss()) {
            AUnit newBuilding = Select.ourUnfinished()
                .ofType(order.buildingType())
                .inRadius(2, unit).nearestTo(unit);
            if (newBuilding != null) {
                order.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
                order.setBuilder(null);
                unit.stop("Finished!", true);
                return false;
            }
        }

        if (AGame.canAfford(buildingType.getMineralPrice(), buildingType.getGasPrice())) {
//            System.err.println("buildPosition PRE = " + order.buildPosition());
//            APosition buildPosition = refreshBuildPosition(order);
            APosition buildPosition = refreshConstructionPositionIfNeeded(order, buildingType);

            if (buildPosition == null) {
                return false;
            }

//            System.err.println("buildPosition POST = " + buildPosition);
//            System.err.println("buildPosition.translateByTiles(1, 1) = " + buildPosition.translateByTiles(1, 1));

            moveOtherUnitsOutOfConstructionPlace(buildPosition.translateByTiles(1, 1));

            // If place is ok, unit isn't constructing, and we can afford it, issue the build command.
            buildPosition = (new GasBuildingFix(unit)).applyGasBuildingFixIfNeeded(buildPosition, buildingType);

            if (buildPosition == null) {
                order.cancel();
                ErrorLog.printMaxOncePerMinute("Cancel construction of " + buildingType + " because position null");
                return false;
            }

            TilePosition buildTilePosition = new TilePosition(
                buildPosition.tx(), buildPosition.ty()
            );

//            if (Select.ourWithUnfinishedOfType(AtlantisRaceConfig.GAS_BUILDING).inRadius(3, buildPosition).notEmpty()) {
//                order.cancel();
//                return false;
//            }

            if (!unit.isConstructing() || unit.isIdle() || AGame.now() % 7 == 0) {
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
        for (AUnit unit : unit.friendsNear().inRadius(2.3, buildPosition).exclude(unit).list()) {
            unit.moveAwayFrom(buildPosition, 1, Actions.MOVE_SPECIAL, "Construction!");
        }
    }

    // =========================================================

    private boolean shouldNotTravelYet(AUnitType building, double distance) {
//        if (AGame.timeSeconds() < 300 && !building.isBase()) {
        if (AGame.timeSeconds() < 300) {
            int baseBonus = building.isBase() ? 80 : 0;
//            return !AGame.canAfford(

//            if (building.is(AUnitType.Zerg_Spawning_Pool)) {
//                System.out.println(
//                    "mineralsRes=" + CurrentProductionQueue.resourcesReserved()[0]
//                    + ", queuePos=" + ProductionQueue.positionInQueue(building)
//                );
//            }

//            if (ProductionQueue.isAtTheTopOfQueue(building, 1)) {
            return !AGame.canAfford(
                building.getMineralPrice() - 32 - (int) (distance * 1.3) - baseBonus,
                building.getGasPrice() - 16 - (int) distance
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
