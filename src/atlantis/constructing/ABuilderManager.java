package atlantis.constructing;

import atlantis.AGame;
import atlantis.constructing.position.AbstractPositionFinder;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import atlantis.util.Us;
import bwapi.TilePosition;

public class ABuilderManager {

    public static void update(AUnit builder) {
        if (builder == null) {
            System.err.println("builder null in ABM.update()");
            return;
        }

        // Don't disturb builder that are already constructing
        if (builder.isConstructing() || builder.isMorphing()) {
            return;
        }

        handleConstruction(builder);
    }

    // =========================================================
    
    private static void handleConstruction(AUnit builder) {
        ConstructionOrder constructionOrder = AConstructionRequests.getConstructionOrderFor(builder);
        if (constructionOrder != null) {

            // Construction HASN'T STARTED YET, we're probably not even at the required place
            if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_NOT_STARTED) {
                travelToConstruct(builder, constructionOrder);
            } else if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS) {
                // Do nothing - construction is pending
            } else if (constructionOrder.getStatus() == ConstructionOrderStatus.CONSTRUCTION_FINISHED) {
                // Do nothing - construction is finished
            }
        } else {
            System.err.println("constructionOrder null for " + builder);
        }
    }

    private static void travelToConstruct(AUnit builder, ConstructionOrder constructionOrder) {
        APosition buildPosition = constructionOrder.getPositionToBuild();
        APosition buildPositionCenter = constructionOrder.getPositionToBuildCenter();
        AUnitType buildingType = constructionOrder.getBuildingType();

        if (builder == null) {
            throw new RuntimeException("Builder empty");
        }
        if (buildPosition == null) {
            System.err.println("buildPosition is null (travelToConstruct " + buildingType + ")");
            constructionOrder.cancel();
            return;
        }

        // =========================================================

        double minDistanceToIssueBuildOrder = buildingType.isGasBuilding() ? 3.6 : 1.1;
        double distance = builder.distTo(buildPositionCenter);
        String distString = "(" + A.digit(distance) + ")";

//        ACamera.centerCameraOn(builder.getPosition());

        // Move builder to the build position
        if (distance > minDistanceToIssueBuildOrder) {
            if (shouldNotTravelYet(buildingType, distance)) {
                builder.setTooltip("Wait to build " + buildingType.shortName() + distString);
                return;
            }

            if (AGame.everyNthGameFrame(3)) {
                if (!builder.isMoving()) {
//                    AGameSpeed.changeSpeedTo(60);
                    builder.move(
                        constructionOrder.getPositionToBuildCenter(),
                        UnitActions.MOVE_TO_BUILD,
                        "Build " + buildingType.shortName() + distString
                    );
                }
            }
        }

        // =========================================================
        // AUnit is already at the build position

        // If we can afford to construct this building exactly right now, issue build order which should
        // be immediate as unit is standing just right there

        else {
            if (Us.isProtoss()) {
                AUnit newBuilding = Select.ourUnfinished()
                        .ofType(constructionOrder.getBuildingType())
                        .inRadius(1.1, builder).first();
                if (newBuilding != null) {
                    constructionOrder.setStatus(ConstructionOrderStatus.CONSTRUCTION_IN_PROGRESS);
                    constructionOrder.setBuilder(null);
                    builder.stop("Finished!");
                    return;
                }
            }

            if (AGame.canAfford(buildingType.getMineralPrice(), buildingType.getGasPrice())) {

                // If place is ok, builder isn't constructing and we can afford it, issue the build command.
                if (AGame.canAfford(buildingType)) {
                    buildPosition = applyGasBuildingFixIfNeeded(builder, buildPosition, buildingType);
                    TilePosition buildTilePosition = new TilePosition(
                            buildPosition.getTileX(), buildPosition.getTileY()
                    );

                    if (buildTilePosition != null && (!builder.isConstructing() || builder.isIdle() ||
                            AGame.getTimeFrames() % 30 == 0)) {
                        builder.build(buildingType, buildTilePosition);
                    }
                }
            }
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
                     building.getMineralPrice() - 2 - (int) distance,
                    building.getGasPrice() - 2- (int) distance
            );
        }

        return false;
    }

}
