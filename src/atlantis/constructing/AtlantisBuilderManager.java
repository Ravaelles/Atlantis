package atlantis.constructing;

import atlantis.AtlantisGame;
import atlantis.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.UnitActions;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APosition;
import bwapi.TilePosition;

public class AtlantisBuilderManager {

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
        ConstructionOrder constructionOrder = AtlantisConstructionManager.getConstructionOrderFor(builder);
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
        //TODO: check possible confusion with Position and TilePosition here
        APosition buildPosition = constructionOrder.getPositionToBuildCenter();
        AUnitType buildingType = constructionOrder.getBuildingType();

        if (builder == null) {
            throw new RuntimeException("Builder empty");
        }
        if (buildPosition == null) {
            System.err.println("buildPosition is null (travelToConstruct " + buildingType + ")");
            constructionOrder.cancel();
            return;
        }

//        buildPosition = PositionUtil.translate(buildPosition, buildingType.getTileWidth() * 16, buildingType.getTileHeight() * 16);
//        buildPosition = PositionUtil.translate(
//                buildPosition, buildingType.getTileWidth() * 32 / 2, buildingType.getTileHeight() * 32 / 2
//        );
        // =========================================================
        double maxDistance = buildingType.isGasBuilding() ? 3 : 1.5;

        // Move builder to the build position
        if (builder.distanceTo(buildPosition) > maxDistance) {
            builder.move(buildPosition, UnitActions.MOVE_TO_BUILD);
        } 

        // =========================================================
        // AUnit is already at the build position, issue build order
        // If we can afford to construct this building exactly right now, issue build order which should
        // be immediate as unit is standing just right there
        else if (AtlantisGame.canAfford(buildingType.getMineralPrice(), buildingType.getGasPrice())) {

            // If place is ok, builder isn't constructing and we can afford it, issue the build command.
            if (buildPosition != null && AtlantisGame.canAfford(buildingType)) {
                buildPosition = applyGasBuildingFixIfNeeded(builder, buildPosition, buildingType);
                TilePosition buildTilePosition = new TilePosition(
                        buildPosition.getTileX(), buildPosition.getTileY()
                );
                
                if (buildTilePosition != null && (!builder.isConstructing() || builder.isIdle())) {
                    builder.build(buildingType, buildTilePosition, UnitActions.BUILD);
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
                            (builder, building, position.translateByTiles(-2, -1))) {
                    System.out.println("Applied [-2,-1] " + building + " position FIX");
                    return position.translateByTiles(-2, -1);
                }
            } 
            return position;
        } else {
            return null;
        }
    }

}
