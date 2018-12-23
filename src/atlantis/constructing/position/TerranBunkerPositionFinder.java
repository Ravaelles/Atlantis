package atlantis.constructing.position;

import atlantis.combat.squad.missions.MissionDefend;
import atlantis.constructing.ConstructionOrder;
import atlantis.information.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwem.Base;
import bwem.ChokePoint;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranBunkerPositionFinder {

    public static APosition findPosition(AUnitType building, AUnit builder, ConstructionOrder order) {
        APosition nearTo = null;
        
        if (order != null && order.getProductionOrder() != null && order.getProductionOrder().getModifier() != null) {
            String locationModifier = order.getProductionOrder().getModifier();
            nearTo = defineBunkerPosition(locationModifier);
        }
        else {
            nearTo = defineBunkerPosition(ASpecialPositionFinder.AT_NATURAL);
        }
        
        // =========================================================
        
        if (nearTo == null) {
            AUnit existingBunker = Select.ourOfType(AUnitType.Terran_Bunker).first();
            if (existingBunker != null) {
                nearTo = existingBunker.getPosition();
                APosition defendPoint = MissionDefend.getInstance().getFocusPoint();
                if (defendPoint != null) {
                    nearTo = nearTo.translatePercentTowards(defendPoint, 15);
                }
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
    
    // =========================================================

    private static APosition defineBunkerPosition(String locationModifier) {
        AUnit mainBase = Select.mainBase();

        // Bunker at MAIN CHOKEPOINT
        if (locationModifier.equals(ASpecialPositionFinder.NEAR_MAIN_CHOKEPOINT)) {
            ChokePoint chokePointForNaturalBase = AMap.getChokePointForMainBase();
            if (chokePointForNaturalBase != null) {
                return APosition.createFrom(chokePointForNaturalBase.getCenter())
                        .translatePercentTowards(mainBase.getPosition(), 5);
            }
        }

        // Bunker at NATURAL CHOKEPOINT
        else {
            ChokePoint chokePointForNaturalBase = AMap.getChokePointForNaturalBase();
            if (chokePointForNaturalBase != null && mainBase != null) {
                Base naturalBase = AMap.getNaturalBase(Select.mainBase().getPosition());
                return APosition.createFrom(chokePointForNaturalBase.getCenter())
                        .translatePercentTowards(naturalBase.getCenter(), 25);

//                    System.out.println();
//                    System.err.println(nearTo);
//                    System.err.println("DIST TO CHOKE = " + nearTo.distanceTo(chokePointForNaturalBase.getCenter()));
//                    System.err.println("DIST TO REGION = " + nearTo.distanceTo(nearTo.getArea().getCenter()));
            }
        }
        
        // Invalid location
        System.err.println("Can't define bunker location: " + locationModifier);
        return null;
    }
    
}
