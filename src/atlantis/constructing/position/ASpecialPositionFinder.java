package atlantis.constructing.position;

import atlantis.constructing.ConstructionOrder;
import atlantis.information.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwta.BaseLocation;

public class ASpecialPositionFinder {
    
    /**
     * Constant used as a hint to indicate that base should be built in the nearest base location 
     * (to the main base) that's still free.
     */
    public static final String BASE_AT_NEAREST_FREE = "NEAREST_FREE";
    
    /**
     * Constant used as a hint to indicate that building should be placed in the main base region.
     */
    public static final String NEAR_MAIN = "MAIN";
    
    /**
     * Constant used as a hint to indicate that building should be placed in the chokepoints of the main base.
     */
    public static final String NEAR_MAIN_CHOKEPOINT = "MAIN_CHOKEPOINT";
    
    /**
     * Constant used as a hint to indicate that building should be placed in the "natural" 
     * (also called the "expansion").
     */
    public static final String AT_NATURAL = "NATURAL";
    
    // =========================================================

    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    protected static APosition findPositionForGasBuilding(AUnitType building) {
        AUnit builder = Select.ourWorkers().first();
        for (AUnit base : Select.ourBases().listUnits()) {
            AUnit geyser = (AUnit) Select.neutral().ofType(AUnitType.Resource_Vespene_Geyser).nearestTo(base);

            if (geyser != null && geyser.distanceTo(base) < 12) {
                APosition position = geyser.getPosition().translateByPixels(-64, -32);
                return position;
            }
        }

        System.err.println("Couldn't find geyser for " + building);
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
            if (modifier.equals(NEAR_MAIN) || modifier.equals("NEAR_MAIN")) {
                return findPositionForBase_nearMainBase(building, builder, constructionOrder);
            }
            else if (modifier.equals(AT_NATURAL)) {
                return findPositionForBase_natural(building, builder, constructionOrder);
            }
        }
        
        return findPositionForBase_nearestFreeBase(building, builder, constructionOrder);
    }

    // =========================================================
    
    private static APosition findPositionForBase_nearestFreeBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        BaseLocation baseLocationToExpand;
        int ourBasesCount = Select.ourBases().count();
        if (ourBasesCount <= 2) {
            baseLocationToExpand = AMap.getExpansionFreeBaseLocationNearestTo(
                    Select.mainBase().getPosition()
            );
        }
        else {
            baseLocationToExpand = AMap.getExpansionBaseLocationMostDistantToEnemy();
        }
        
        if (baseLocationToExpand == null) {
            if (ourBasesCount <= 2) {
                System.err.println("baseLocationToExpand is null");
            }
            return null;
        }
        
        APosition near = APosition.create(baseLocationToExpand.getPosition()).translateByPixels(-64, -48);
        constructionOrder.setMaxDistance(1);

//        System.out.println("Main base = " + Select.mainBase());
//        System.out.println("baseLocationToExpand = " + baseLocationToExpand);
//        System.out.println(builder + " / " + building + " / " +  APosition.createFrom(baseLocationToExpand.getPosition()));

        return APositionFinder.findStandardPosition(
                builder, building, near, constructionOrder.getMaxDistance()
        );
    }

    private static APosition findPositionForBase_nearMainBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        APosition near = Select.mainBase().getPosition().translateByPixels(-64, -48);
        
        constructionOrder.setNearTo(near);
        constructionOrder.setMaxDistance(30);
        
        return APositionFinder.findStandardPosition(builder, building, near, constructionOrder.getMaxDistance());
    }

    private static APosition findPositionForBase_natural(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        APosition near = APosition.create(AMap.getExpansionFreeBaseLocationNearestTo(Select.mainBase().getPosition()).getPosition()
        ).translateByPixels(-64, -48);
        
        constructionOrder.setNearTo(near);
        constructionOrder.setMaxDistance(1);
        
        return APositionFinder.findStandardPosition(builder, building, near, constructionOrder.getMaxDistance());
    }

}
