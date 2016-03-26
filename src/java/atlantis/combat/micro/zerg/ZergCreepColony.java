package atlantis.combat.micro.zerg;

import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.position.AtlantisPositionFinder;
import atlantis.wrappers.Select;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ZergCreepColony {

    public static Position findPosition(UnitType building, Unit builder, ConstructionOrder constructionOrder) {
        Unit secondBase = Select.secondBaseOrMainIfNoSecond();
        if (secondBase != null) {
            return AtlantisPositionFinder.findStandardPosition(builder, building, secondBase.getPosition(), 10);
        }
        else {
            return null;
        }
    }
    
    // =========================================================

    public static void creepOneIntoSunkenColony() {
        Unit creepColony = (Unit) Select.ourBuildings().ofType(UnitType.Zerg_Creep_Colony).first();
        if (creepColony != null) {
            creepColony.morph(UnitType.Zerg_Sunken_Colony);
        }
    }
    
}
