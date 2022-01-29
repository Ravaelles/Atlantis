package atlantis.production.constructing.position;

import atlantis.combat.missions.defend.MissionDefend;
import atlantis.map.ABaseLocation;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.ConstructionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;


public class TerranBunkerPositionFinder {

    public static APosition findPosition(AUnitType building, AUnit builder, ConstructionOrder order) {
        HasPosition nearTo = null;
        
        if (order != null && order.productionOrder() != null && order.productionOrder().getModifier() != null) {
            String locationModifier = order.productionOrder().getModifier();
            nearTo = defineBunkerPosition(locationModifier);
        }
        else {
            nearTo = defineBunkerPosition(ASpecialPositionFinder.AT_NATURAL);
        }
        
        // =========================================================
        
        if (nearTo == null) {
            AUnit existingBunker = Select.ourOfType(AUnitType.Terran_Bunker).first();
            if (existingBunker != null) {
                nearTo = existingBunker.position();
                APosition defendPoint = MissionDefend.getInstance().focusPoint();
                if (defendPoint != null) {
                    nearTo = nearTo.translatePercentTowards(defendPoint, 15);
                }
            }
            else {
                AUnit mainBase = Select.main();
                if (mainBase != null) {
                    nearTo = Select.main().position();
                }
            }
        }
        
        // =========================================================
        // Find position near specified place
        return APositionFinder.findStandardPosition(builder, building, nearTo, 30);
    }
    
    // =========================================================

    private static APosition defineBunkerPosition(String locationModifier) {
        AUnit mainBase = Select.main();
        if (mainBase == null) {
            return null;
        }

        // Bunker at MAIN CHOKEPOINT
        if (locationModifier.equals(ASpecialPositionFinder.NEAR_MAIN_CHOKEPOINT)) {
            AChoke chokepointForNatural = Chokes.mainChoke();
            if (chokepointForNatural != null) {
                return APosition.create(chokepointForNatural.center())
                        .translatePercentTowards(mainBase.position(), 5);
            }
        }

        // Bunker at NATURAL CHOKEPOINT
        else {
            AChoke chokepointForNatural = Chokes.natural(mainBase.position());
            if (chokepointForNatural != null && mainBase != null) {
                ABaseLocation natural = Bases.natural(Select.main().position());
                return APosition.create(chokepointForNatural.center())
                        .translatePercentTowards(natural, 25);

//                    System.out.println();
//                    System.err.println(nearTo);
//                    System.err.println("DIST TO CHOKE = " + nearTo.distanceTo(chokepointForNatural.getCenter()));
//                    System.err.println("DIST TO REGION = " + nearTo.distanceTo(nearTo.getRegion().getCenter()));
            }
        }
        
        // Invalid location
        System.err.println("Can't define bunker location: " + locationModifier);
        return null;
    }
    
}
