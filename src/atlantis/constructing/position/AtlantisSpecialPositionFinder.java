package atlantis.constructing.position;

import atlantis.AtlantisGame;
import atlantis.constructing.ConstructionOrder;
import atlantis.debug.AtlantisPainter;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APosition;
import bwapi.Color;
import bwta.BaseLocation;

public class AtlantisSpecialPositionFinder {
    
    /**
     * Constant used as a hint to indicate that base should be built in the nearest base location 
     * (to the main base) that's still free.
     */
    public static final String BASE_NEAREST_FREE = "NEAREST_FREE";
    
    /**
     * Constant used as a hint to indicate that base should be built in the main base (not as much 
     * expansion as additional slots in base).
     */
    public static final String BASE_NEAR_MAIN = "MAIN";
    
    /**
     * Constant used as a hint to indicate that base should be built in the "natural" 
     * (also called "expansion").
     */
    public static final String BASE_NATURAL = "NATURAL";
    
    // =========================================================

    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    protected static APosition findPositionForGasBuilding(AUnitType building) {
        for (AUnit base : Select.ourBases().listUnits()) {
            AUnit geyser = (AUnit) Select.neutral().ofType(AUnitType.Resource_Vespene_Geyser).nearestTo(base.getPosition());

            if (geyser != null && geyser.distanceTo(base) < 12) {
                return PositionUtil.translate(geyser.getPosition(), -48, -32);
            }
        }

        return null;
    }

    /**
     * Returns build position for next base. It will usually be next free BaseLocation that doesn't have base
     * built.
     */
    public static APosition findPositionForBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        String modifier = constructionOrder.getProductionOrder() != null ? 
                constructionOrder.getProductionOrder().getModifier() : null;
        
//        System.err.println("");
//        System.err.println(constructionOrder.getProductionOrder());
//        System.err.println("=== modifier /" + modifier + "/ ===");
        if (modifier != null) {
            if (modifier.equals(BASE_NEAR_MAIN) || modifier.equals("NEAR_MAIN")) {
                return findPositionForBase_nearMainBase(building, builder, constructionOrder);
            }
            else if (modifier.equals(BASE_NATURAL)) {
                return findPositionForBase_natural(building, builder, constructionOrder);
            }
        }
        
        return findPositionForBase_nearestFreeBase(building, builder, constructionOrder);
    }

    // =========================================================
    
    private static APosition findPositionForBase_nearestFreeBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        BaseLocation baseLocationToExpand;
        if (Select.ourBases().count() <= 2) {
            baseLocationToExpand = AtlantisMap.getExpansionFreeBaseLocationNearestTo(
                    Select.mainBase().getPosition().translateByPixels(-64, -48)
            );
        }
        else {
            baseLocationToExpand = AtlantisMap.getExpansionBaseLocationMostDistantToEnemy();
        }
        
        if (baseLocationToExpand == null) {
            System.err.println("baseLocationToExpand is null");
            return null;
        }
        
        APosition near = APosition.createFrom(baseLocationToExpand.getPosition());
        constructionOrder.setMaxDistance(4);

//        System.out.println("Main base = " + Select.mainBase());
//        System.out.println("baseLocationToExpand = " + baseLocationToExpand);
//        System.out.println(builder + " / " + building + " / " +  APosition.createFrom(baseLocationToExpand.getPosition()));

        return AtlantisPositionFinder.findStandardPosition(
                builder, building, near, constructionOrder.getMaxDistance()
        );
    }

    private static APosition findPositionForBase_nearMainBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        APosition near = Select.mainBase().getPosition().translateByPixels(-64, -48);
        
        constructionOrder.setNearTo(near);
        constructionOrder.setMaxDistance(30);
        
        return AtlantisPositionFinder.findStandardPosition(builder, building, near, constructionOrder.getMaxDistance());
    }

    private static APosition findPositionForBase_natural(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        APosition near = APosition.createFrom(
                AtlantisMap.getExpansionFreeBaseLocationNearestTo(Select.mainBase().getPosition()).getPosition()
        ).translateByPixels(-64, -48);
        
        constructionOrder.setNearTo(near);
        constructionOrder.setMaxDistance(4);
        
        return AtlantisPositionFinder.findStandardPosition(builder, building, near, constructionOrder.getMaxDistance());
    }

}
