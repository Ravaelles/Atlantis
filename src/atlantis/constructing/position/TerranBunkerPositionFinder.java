package atlantis.constructing.position;

import atlantis.combat.missions.MissionDefend;
import atlantis.constructing.ConstructionOrder;
import atlantis.map.ABaseLocation;
import atlantis.map.AChoke;
import atlantis.map.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;


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
                APosition defendPoint = MissionDefend.getInstance().focusPoint();
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
        if (mainBase == null) {
            return null;
        }

        // Bunker at MAIN CHOKEPOINT
        if (locationModifier.equals(ASpecialPositionFinder.NEAR_MAIN_CHOKEPOINT)) {
            AChoke chokepointForNaturalBase = AMap.getChokeForMainBase();
            if (chokepointForNaturalBase != null) {
                return APosition.create(chokepointForNaturalBase.getCenter())
                        .translatePercentTowards(mainBase.getPosition(), 5);
            }
        }

        // Bunker at NATURAL CHOKEPOINT
        else {
            AChoke chokepointForNaturalBase = AMap.getChokeForNaturalBase(mainBase.getPosition());
            if (chokepointForNaturalBase != null && mainBase != null) {
                ABaseLocation naturalBase = AMap.getNaturalBaseLocation(Select.mainBase().getPosition());
                return APosition.create(chokepointForNaturalBase.getCenter())
                        .translatePercentTowards(naturalBase, 25);

//                    System.out.println();
//                    System.err.println(nearTo);
//                    System.err.println("DIST TO CHOKE = " + nearTo.distanceTo(chokepointForNaturalBase.getCenter()));
//                    System.err.println("DIST TO REGION = " + nearTo.distanceTo(nearTo.getRegion().getCenter()));
            }
        }
        
        // Invalid location
        System.err.println("Can't define bunker location: " + locationModifier);
        return null;
    }
    
}
