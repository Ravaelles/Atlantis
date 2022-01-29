package atlantis.production.constructing;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.production.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.We;
import bwapi.TilePosition;

public class ABuilderManager {

    public static boolean update(AUnit builder) {

        // Sometimes an ugly thing like this may happen
        if (We.terran() && builder.isConstructing() && builder.buildUnit() != null && A.everyNthGameFrame(32)) {
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
        ConstructionOrder constructionOrder = ConstructionRequests.constructionOrderFor(builder);
        if (constructionOrder != null) {

            // Construction HASN'T STARTED YET, we're probably not even at the required place
            if (constructionOrder.status() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                return travelToConstruct(builder, constructionOrder);
            } else if (constructionOrder.status() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS) {
                // Do nothing - construction is pending
            } else if (constructionOrder.status() == ConstructionOrderStatus.CONSTRUCTION_FINISHED) {
                // Do nothing - construction is finished
            }
        } else {
            System.err.println("constructionOrder null for " + builder);
            return false;
        }
        return false;
    }

    private static boolean travelToConstruct(AUnit builder, ConstructionOrder constructionOrder) {
        APosition buildPosition = constructionOrder.positionToBuild();
        APosition buildPositionCenter = constructionOrder.positionToBuildCenter();
        AUnitType buildingType = constructionOrder.buildingType();

        if (builder == null) {
            throw new RuntimeException("Builder empty");
        }
        if (buildPosition == null) {
            System.err.println("buildPosition is null (travelToConstruct " + buildingType + ")");
            constructionOrder.cancel();
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
//                    GameSpeed.changeSpeedTo(60);
                builder.move(
                    constructionOrder.positionToBuildCenter(),
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
            if (We.protoss()) {
                AUnit newBuilding = Select.ourUnfinished()
                        .ofType(constructionOrder.buildingType())
                        .inRadius(1.1, builder).first();
                if (newBuilding != null) {
                    constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
                    constructionOrder.setBuilder(null);
                    builder.stop("Finished!", true);
                    return false;
                }
            }

            if (AGame.canAfford(buildingType.getMineralPrice(), buildingType.getGasPrice())) {

                // If place is ok, builder isn't constructing and we can afford it, issue the build command.
                if (AGame.canAfford(buildingType)) {
                    buildPosition = applyGasBuildingFixIfNeeded(builder, buildPosition, buildingType);
                    TilePosition buildTilePosition = new TilePosition(
                            buildPosition.tx(), buildPosition.ty()
                    );

                    if (buildTilePosition != null && (!builder.isConstructing() || builder.isIdle() ||
                            AGame.now() % 30 == 0)) {
                        builder.build(buildingType, buildTilePosition);
                        return true;
                    }
                }
            }

            return true;
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
                
                System.err.println("Gas building FIX was not applied");
                System.err.println("This probably halts gas building");
            } 
            return position;
        } else {
            return null;
        }
    }

    // =========================================================

    private static boolean shouldNotTravelYet(AUnitType building, double distance) {
        if (AGame.timeSeconds() < 200 && !building.isBase()) {
            return !AGame.canAfford(
                     building.getMineralPrice() - 24 - (int) distance,
                    building.getGasPrice() - 16 - (int) distance
            );
        }

        return false;
    }

}
