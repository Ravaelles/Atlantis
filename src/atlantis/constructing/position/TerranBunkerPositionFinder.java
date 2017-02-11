package atlantis.constructing.position;

import atlantis.constructing.ConstructionOrder;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.wrappers.APosition;
import bwta.Chokepoint;
import bwta.Region;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranBunkerPositionFinder {

    public static APosition findPosition(AUnitType building, AUnit builder, ConstructionOrder order) {
        APosition nearTo = null;
        
        // Bunker at NATURAL CHOKEPOINT
        if (order.getProductionOrder().getModifier() == AtlantisSpecialPositionFinder.AT_NATURAL) {
            Chokepoint chokepointForNaturalBase = AtlantisMap.getChokepointForNaturalBase();
            if (chokepointForNaturalBase != null) {
                nearTo = APosition.create(chokepointForNaturalBase.getCenter()).translateTowardCenterOfRegion(10);
            }
        }
        
        // Bunker at MAIN CHOKEPOINT
        else if (order.getProductionOrder().getModifier() == AtlantisSpecialPositionFinder.NEAR_MAIN_CHOKEPOINT) {
            Chokepoint chokepointForNaturalBase = AtlantisMap.getChokepointForMainBase();
            if (chokepointForNaturalBase != null) {
                nearTo = APosition.create(chokepointForNaturalBase.getCenter()).translateTowardCenterOfRegion(5);
            }
        }
        
        // =========================================================
        
        if (nearTo == null && Select.mainBase() != null) {
            nearTo = Select.mainBase().getPosition();
        }
        
        // =========================================================
        // Find position near specified place
        return AtlantisPositionFinder.findStandardPosition(builder, building, nearTo, 30);
    }
    
}
