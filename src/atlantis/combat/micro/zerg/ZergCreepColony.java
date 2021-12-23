package atlantis.combat.micro.zerg;

import atlantis.production.constructing.ConstructionOrder;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;


public class ZergCreepColony {

    public static APosition findPosition(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        AUnit secondBase = Select.naturalOrMain();
        if (secondBase != null) {
            return APositionFinder.findStandardPosition(builder, building, secondBase.position(), 10);
        }
        else {
            return null;
        }
    }
    
    // =========================================================

    public static void creepOneIntoSunkenColony() {
        AUnit creepColony = Select.ourBuildings().ofType(AUnitType.Zerg_Creep_Colony).first();
        if (creepColony != null) {
            creepColony.morph(AUnitType.Zerg_Sunken_Colony);
        }
    }
    
}
