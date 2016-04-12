package atlantis.constructing.position;

import atlantis.constructing.ConstructionOrder;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.PositionUtil;
import atlantis.wrappers.APosition;
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
    public static final String BASE_NEAR_MAIN = "NEAR_MAIN";
    
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

            if (geyser != null && geyser.distanceTo(base) < 10) {
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
//        String mode = "NEAREST_FREE";
//        String mode = "NEAR_MAIN";
        String mode = constructionOrder.getProductionOrder() != null ? 
                constructionOrder.getProductionOrder().getModifier() : null;
        
        if (mode != null) {
            if (mode.equals(BASE_NEAR_MAIN)) {
                return findPositionForBase_nearestMainBase(building, builder);
            }
            else if (mode.equals(BASE_NATURAL)) {
                return findPositionForBase_natural(building, builder);
            }
        }
        
        return findPositionForBase_nearestFreeBase(building, builder);
    }

    // =========================================================
    
    private static APosition findPositionForBase_nearestFreeBase(AUnitType building, AUnit builder) {
        BaseLocation baseLocationToExpand = AtlantisMap.getNearestBaseLocationToExpand(Select.mainBase().getPosition());
        if (baseLocationToExpand == null) {
            System.err.println("baseLocationToExpand is null");
            return null;
        }

//        System.out.println("Main base = " + Select.mainBase());
//        System.out.println("baseLocationToExpand = " + baseLocationToExpand);

        return AtlantisPositionFinder.findStandardPosition(builder, building, 
                APosition.createFrom(baseLocationToExpand.getPosition()), 1);
    }

    private static APosition findPositionForBase_nearestMainBase(AUnitType building, AUnit builder) {
        return AtlantisPositionFinder.findStandardPosition(builder, building, Select.mainBase().getPosition(), 20);
    }

    private static APosition findPositionForBase_natural(AUnitType building, AUnit builder) {
        APosition nearestBaseLocation = APosition.createFrom(
                AtlantisMap.getNearestBaseLocationToExpand(Select.mainBase().getPosition()).getPosition()
        );
        return AtlantisPositionFinder.findStandardPosition(builder, building, nearestBaseLocation, 3);
    }

}
