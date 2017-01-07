package atlantis.constructing;

import atlantis.AtlantisGame;
import atlantis.constructing.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.missions.UnitMissions;
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
        APosition buildPosition = constructionOrder.getPositionToBuild();
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
        
        double maxDistance = buildingType.isGasBuilding() ? 5 : 1;
        
        // Move builder to the build position
        if (builder.distanceTo(buildPosition) > maxDistance) {
            builder.move(buildPosition, UnitMissions.BUILD);
        } 

        // AUnit is already at the build position, issue build order
        // If we can afford to construct this building exactly right now, issue build order which should
        // be immediate as unit is standing just right there
        else if (AtlantisGame.canAfford(buildingType.getMineralPrice(), buildingType.getGasPrice())) {
//            if (!AbstractPositionFinder.canPhysicallyBuildHere(builder, buildingType, buildPosition)) {
//                buildPosition = constructionOrder.findNewBuildPosition();
//            }

            // If place is ok, builder isn't constructing and we can afford it, issue the build command.
            if (buildPosition != null && AtlantisGame.canAfford(buildingType)) {
//                buildPosition = constructionOrder.findNewBuildPosition();
                TilePosition buildTilePosition = buildPosition.toTilePosition();
                if (buildTilePosition != null && !builder.isConstructing()) {
                    builder.build(buildingType, buildTilePosition, UnitMissions.BUILD);
                }
            }
        }
    }

}
