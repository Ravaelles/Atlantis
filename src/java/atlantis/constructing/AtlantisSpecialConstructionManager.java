package atlantis.constructing;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.combat.micro.zerg.ZergBase;
import atlantis.production.ProductionOrder;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
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
        
        else if (building.isType(UnitType.UnitTypes.Zerg_Lair)) {
            ZergBase.creepOneHatcheryIntoLair();
            return true;
        }
        
        else if (building.isType(UnitType.UnitTypes.Zerg_Hive)) {
            ZergBase.creepOneLairIntoHive();
            return true;
        }
        
        else if (building.isType(UnitType.UnitTypes.Zerg_Greater_Spire)) {
            morphFromZergBuildingInto(UnitType.UnitTypes.Zerg_Spire, UnitType.UnitTypes.Zerg_Greater_Spire);
            return true;
        }
        
        return false;
    }
    
    // =========================================================

    private static void morphFromZergBuildingInto(UnitType from, UnitType into) {
        Unit building = SelectUnits.ourBuildings().ofType(from).first();
        if (building == null) {
            System.err.println("No " + from + " found to morph into " + into);
        }
        else {
            building.morph(into);
        }
    }
    
}
