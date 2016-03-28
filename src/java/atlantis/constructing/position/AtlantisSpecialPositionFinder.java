package atlantis.constructing.position;

import atlantis.constructing.ConstructionOrder;
import atlantis.enemy.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.PositionUtil;
import bwapi.Position;

import bwapi.UnitType;
import atlantis.units.Select;
import bwta.BaseLocation;

public class AtlantisSpecialPositionFinder {
    
    /**
     * Constant used as a hint to indicate that base should be built in the nearest base location 
     * (to the main base) that's still free.
     */
    public static final String NEW_BASE_NEAREST_FREE = "NEAREST_FREE";
    
    /**
     * Constant used as a hint to indicate that base should be built in the main base (not as much 
     * expansion as additional slots in base).
     */
    public static final String NEW_BASE_NEAR_MAIN = "NEAR_MAIN";
    
    // =========================================================

    /**
     * Returns build position for next Refinery/Assimilator/Extractor. It will be chosen for the oldest base
     * that doesn't have gas extracting building.
     */
    protected static Position findPositionForGasBuilding(AUnitType building) {
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
    public static Position findPositionForBase(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
//        String mode = "NEAREST_FREE";
//        String mode = "NEAR_MAIN";
        String mode = constructionOrder.getProductionOrder() != null ? 
                constructionOrder.getProductionOrder().getModifier() : null;
        
        if (mode != null) {
            if (mode.equals(NEW_BASE_NEAR_MAIN)) {
                return findPositionForBase_nearestMainBase(building, builder);
            }
        }
        
        return findPositionForBase_nearestFreeBase(building, builder);
    }

    // =========================================================
    
    private static Position findPositionForBase_nearestFreeBase(AUnitType building, AUnit builder) {
        BaseLocation baseLocationToExpand = AtlantisMap.getNearestBaseLocationToExpand(Select.mainBase().getPosition());
        if (baseLocationToExpand == null) {
            System.err.println("baseLocationToExpand is null");
            return null;
        }

//        System.out.println("Main base = " + Select.mainBase());
//        System.out.println("baseLocationToExpand = " + baseLocationToExpand);

        return AtlantisPositionFinder.findStandardPosition(builder, building, baseLocationToExpand.getPosition(), 3);
    }

    private static Position findPositionForBase_nearestMainBase(AUnitType building, AUnit builder) {
        return AtlantisPositionFinder.findStandardPosition(builder, building, Select.mainBase().getPosition(), 20);
    }

}
