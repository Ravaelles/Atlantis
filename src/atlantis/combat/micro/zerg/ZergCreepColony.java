package atlantis.combat.micro.zerg;

import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.position.APositionFinder;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ZergCreepColony {

    public static APosition findPosition(AUnitType building, AUnit builder, ConstructionOrder constructionOrder) {
        AUnit secondBase = Select.secondBaseOrMainIfNoSecond();
        if (secondBase != null) {
            return APositionFinder.findStandardPosition(builder, building, secondBase.getPosition(), 10);
        }
        else {
            return null;
        }
    }
    
    // =========================================================

    public static void creepOneIntoSunkenColony() {
        AUnit creepColony = (AUnit) Select.ourBuildings().ofType(AUnitType.Zerg_Creep_Colony).first();
        if (creepColony != null) {
            creepColony.morph(AUnitType.Zerg_Sunken_Colony);
        }
    }
    
}
