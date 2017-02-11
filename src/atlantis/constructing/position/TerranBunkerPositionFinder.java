package atlantis.constructing.position;

import atlantis.constructing.ConstructionOrder;
import atlantis.information.AtlantisMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.wrappers.APosition;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranBunkerPositionFinder {

    public static APosition findPosition(AUnitType building, AUnit builder, ConstructionOrder order) {
        APosition nearTo = null;
        
        if (order.getProductionOrder().getModifier() != null) {
            AUnit mainBase = Select.mainBase();
        
            // Bunker at NATURAL CHOKEPOINT
            if (order.getProductionOrder().getModifier().equals(AtlantisSpecialPositionFinder.AT_NATURAL)) {
                Chokepoint chokepointForNaturalBase = AtlantisMap.getChokepointForNaturalBase();
                if (chokepointForNaturalBase != null && mainBase != null) {
                    BaseLocation naturalBase = AtlantisMap.getNaturalBaseLocation(Select.mainBase().getPosition());
                    nearTo = APosition.create(chokepointForNaturalBase.getCenter())
                            .translateTowards(naturalBase, 25);

//                    System.out.println();
//                    System.err.println(nearTo);
//                    System.err.println("DIST TO CHOKE = " + nearTo.distanceTo(chokepointForNaturalBase.getCenter()));
//                    System.err.println("DIST TO REGION = " + nearTo.distanceTo(nearTo.getRegion().getCenter()));
                }
            }

            // Bunker at MAIN CHOKEPOINT
            else if (order.getProductionOrder().getModifier().equals(AtlantisSpecialPositionFinder.NEAR_MAIN_CHOKEPOINT)) {
                Chokepoint chokepointForNaturalBase = AtlantisMap.getChokepointForMainBase();
                if (chokepointForNaturalBase != null) {
                    nearTo = APosition.create(chokepointForNaturalBase.getCenter())
                            .translateTowards(mainBase.getPosition(), 5);
                }
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
