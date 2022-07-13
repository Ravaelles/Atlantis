package atlantis.production.constructing;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.config.AtlantisConfig;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;
import bwapi.TilePosition;

public class ABuilderManager {

    public static boolean update(AUnit builder) {
        if (AvoidEnemies.avoidEnemiesIfNeeded(builder)) {
            return true;
        }

        // Sometimes an ugly thing like this may happen
        if (We.terran() && builder.isConstructing() && builder.buildUnit() != null && A.everyNthGameFrame(29)) {
            builder.doRightClickAndYesIKnowIShouldAvoidUsingIt(builder.buildUnit());
            return true;
        }

        // Don't disturb builder that are already constructing
        if (builder.isConstructing() || builder.isMorphing()) {
            return true;
        }

        if (handleConstruction(builder)) {
            return true;
        }

        return false;
    }

    // =========================================================
    
    private static boolean handleConstruction(AUnit builder) {
        Construction construction = ConstructionRequests.constructionFor(builder);
        if (construction != null) {

            // Construction HASN'T STARTED YET, we're probably not even at the required place
            if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                return travelToConstruct(builder, construction);
            } else if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS) {
                // Do nothing - construction is pending
            } else if (construction.status() == ConstructionOrderStatus.CONSTRUCTION_FINISHED) {
                // Do nothing - construction is finished
            }
        } else {
            System.err.println("construction null for " + builder);
            return false;
        }
        return false;
    }

    private static boolean travelToConstruct(AUnit builder, Construction construction) {
        APosition buildPosition = construction.buildPosition();
        APosition buildPositionCenter = construction.positionToBuildCenter();
        AUnitType buildingType = construction.buildingType();

        if (builder == null) {
            throw new RuntimeException("Builder empty");
        }
        if (buildPosition == null) {
            System.err.println("buildPosition is null (travelToConstruct " + buildingType + ")");
            construction.cancel();
            return false;
        }

        // =========================================================

        double minDistanceToIssueBuildOrder = buildingType.isGasBuilding() ? 3.6 : 1.1;
        double distance = builder.distTo(buildPositionCenter);
        String distString = "(" + A.digit(distance) + ")";

//        CameraManager.centerCameraOn(builder.getPosition());

        // Move builder to the build position
        if (distance > minDistanceToIssueBuildOrder) {
            if (shouldNotTravelYet(buildingType, distance)) {
                builder.setTooltipTactical("Wait to build " + buildingType.name() + distString);
                return false;
            }

            if (!builder.isMoving()) {
//                if (A.everyNthGameFrame(20)) {
//                    construction.setPositionToBuild(newPosition);
//                }

//                    GameSpeed.changeSpeedTo(60);
                builder.move(
                    construction.positionToBuildCenter(),
                    Actions.MOVE_BUILD,
                    "Build " + buildingType.name() + distString,
                    true
                );
            }

            return true;
        }

        // =========================================================
        // AUnit is already at the build position

        // If we can afford to construct this building exactly right now, issue build order which should
        // be immediate as unit is standing just right there

        else {
            return issueBuildOrder(builder, buildingType, construction);
        }
    }

    private static boolean issueBuildOrder(
        AUnit builder, AUnitType buildingType, Construction order
    ) {
        if (We.protoss()) {
            AUnit newBuilding = Select.ourUnfinished()
                .ofType(order.buildingType())
                .inRadius(2, builder).nearestTo(builder);
            if (newBuilding != null) {
                order.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
                order.setBuilder(null);
                builder.stop("Finished!", true);
                return false;
            }
        }

        if (AGame.canAfford(buildingType.getMineralPrice(), buildingType.getGasPrice())) {
//            System.err.println("buildPosition PRE = " + order.buildPosition());
            APosition buildPosition = refreshBuildPosition(order);
//            APosition buildPosition = order.buildPosition();

            if (buildPosition == null) {
                return false;
            }

//            System.err.println("buildPosition POST = " + buildPosition);
//            System.err.println("buildPosition.translateByTiles(1, 1) = " + buildPosition.translateByTiles(1, 1));

            moveOtherUnitsOutOfConstructionPlace(builder, buildPosition.translateByTiles(1, 1));

            // If place is ok, builder isn't constructing and we can afford it, issue the build command.
            buildPosition = applyGasBuildingFixIfNeeded(builder, buildPosition, buildingType);
            TilePosition buildTilePosition = new TilePosition(
                buildPosition.tx(), buildPosition.ty()
            );

            if (Select.ourWithUnfinishedOfType(AtlantisConfig.GAS_BUILDING).inRadius(3, buildPosition).notEmpty()) {
                order.cancel();
                return false;
            }

            if (!builder.isConstructing() || builder.isIdle() || AGame.now() % 7 == 0) {
                builder.build(buildingType, buildTilePosition);
                return true;
            }
        }

        return true;
    }

    private static APosition refreshBuildPosition(Construction order) {
        if (Select.ourWorkers().inRadius(1.8, order.buildPosition()).atLeast(2)) {
            return APositionFinder.findStandardPosition(
                order.builder(), order.buildingType(), order.buildPosition(), 10
            );
//            return APositionFinder.findPositionForNew(
//                order.builder(), order.buildingType(), order
//            );
        }

        return order.buildPosition();
    }

    private static void moveOtherUnitsOutOfConstructionPlace(AUnit builder, APosition buildPosition) {
        for (AUnit unit : builder.friendsNear().inRadius(2.3, buildPosition).exclude(builder).list()) {
            unit.moveAwayFrom(buildPosition, 1, "Construction!", Actions.MOVE_SPECIAL);
        }
    }

    /**
     * From reasons impossible to explain sometimes it happens that we need to build Extractor
     * one tile left from the top left position. Sometimes not. This method takes care of
     * these cases and ensures the position is valid.
     */
    private static APosition applyGasBuildingFixIfNeeded(AUnit builder, APosition position, AUnitType building) {
        if (position != null) {
            if (building.isGasBuilding()
                    && !AbstractPositionFinder.canPhysicallyBuildHere(builder, building, position)) {
                if (AbstractPositionFinder.canPhysicallyBuildHere
                            (builder, building, position.translateByTiles(-1, 0))) {
                    System.out.println("Applied [-1,0] " + building + " position FIX");
                    return position.translateByTiles(-1, 0);
                }
                if (AbstractPositionFinder.canPhysicallyBuildHere
                            (builder, building, position.translateByTiles(1, 0))) {
                    System.out.println("Applied [1,0] " + building + " position FIX");
                    return position.translateByTiles(1, 0);
                }
                if (AbstractPositionFinder.canPhysicallyBuildHere
                            (builder, building, position.translateByTiles(-2, -1))) {
                    System.out.println("Applied [-2,-1] " + building + " position FIX");
                    return position.translateByTiles(-2, -1);
                }
                if (AbstractPositionFinder.canPhysicallyBuildHere
                            (builder, building, position.translateByTiles(2, 1))) {
                    System.out.println("Applied [2,1] " + building + " position FIX");
                    return position.translateByTiles(2, 1);
                }

                System.err.println("Gas building FIX was not applied. This probably halts gas building");
            } 
            return position;
        } else {
            return null;
        }
    }

    // =========================================================

    private static boolean shouldNotTravelYet(AUnitType building, double distance) {
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
