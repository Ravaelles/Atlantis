package atlantis.combat.micro.zerg;

import atlantis.constructing.ConstructionOrder;
import atlantis.constructing.position.AtlantisPositionFinder;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ZergCreepColony {

    public static Position findPosition(UnitType building, Unit builder, ConstructionOrder constructionOrder) {
        Unit secondBase = SelectUnits.secondBaseOrMainIfNoSecond();
        if (secondBase != null) {
            return AtlantisPositionFinder.findStandardPosition(builder, building, secondBase, 10);
        }
        else {
            return null;
        }
    }
    
    // =========================================================

    public static void creepOneIntoSunkenColony() {
        Unit creepColony = SelectUnits.ourBuildings().ofType(UnitType.UnitTypes.Zerg_Creep_Colony).first();
        if (creepColony != null) {
            creepColony.morph(UnitType.UnitTypes.Zerg_Sunken_Colony);
        }
    }
    
}
