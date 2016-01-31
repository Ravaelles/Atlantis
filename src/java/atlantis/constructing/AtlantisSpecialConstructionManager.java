package atlantis.constructing;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.production.ProductionOrder;
import jnibwapi.types.UnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisSpecialConstructionManager {

    /**
     * Some buildings like Zerg SUnken Colony need special treatment.
     */
    protected static boolean handledAsSpecialBuilding(UnitType building, ProductionOrder order) {
        if (building.equals(UnitType.UnitTypes.Zerg_Sunken_Colony)) {
            ZergCreepColony.creepOneIntoSunkenColony();
            return true;
        }
        
        return false;
    }
    
}
