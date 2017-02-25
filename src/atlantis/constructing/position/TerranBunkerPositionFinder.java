package atlantis.constructing.position;

import atlantis.constructing.ConstructionOrder;
import atlantis.information.AMap;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.position.APosition;
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
        
        if (order.getProductionOrder() != null && order.getProductionOrder().getModifier() != null) {
            AUnit mainBase = Select.mainBase();
        
            // Bunker at NATURAL CHOKEPOINT
            if (order.getProductionOrder().getModifier().equals(ASpecialPositionFinder.AT_NATURAL)) {
                Chokepoint chokepointForNaturalBase = AMap.getChokepointForNaturalBase();
                if (chokepointForNaturalBase != null && mainBase != null) {
                    BaseLocation naturalBase = AMap.getNaturalBaseLocation(Select.mainBase().getPosition());
                    nearTo = APosition.create(chokepointForNaturalBase.getCenter())
                            .translateTowards(naturalBase, 25);

//                    System.out.println();
//                    System.err.println(nearTo);
//                    System.err.println("DIST TO CHOKE = " + nearTo.distanceTo(chokepointForNaturalBase.getCenter()));
//                    System.err.println("DIST TO REGION = " + nearTo.distanceTo(nearTo.getRegion().getCenter()));
                }
            }

            // Bunker at MAIN CHOKEPOINT
            else if (order.getProductionOrder().getModifier().equals(ASpecialPositionFinder.NEAR_MAIN_CHOKEPOINT)) {
                Chokepoint chokepointForNaturalBase = AMap.getChokepointForMainBase();
                if (chokepointForNaturalBase != null) {
                    nearTo = APosition.create(chokepointForNaturalBase.getCenter())
                            .translateTowards(mainBase.getPosition(), 5);
                }
            }
        }
        
        // =========================================================
        
        if (nearTo == null) {
            AUnit existingBunker = Select.ourOfType(AUnitType.Terran_Bunker).first();
            if (existingBunker != null) {
                nearTo = existingBunker.getPosition();
            }
            else {
                AUnit mainBase = Select.mainBase();
                if (mainBase != null) {
                    nearTo = Select.mainBase().getPosition();
                }
            }
        }
        
        // =========================================================
        // Find position near specified place
        return APositionFinder.findStandardPosition(builder, building, nearTo, 30);
    }
    
}
