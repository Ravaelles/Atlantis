package atlantis.constructing;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.production.ProductionOrder;
import atlantis.wrappers.Select;
import bwapi.Unit;
import bwapi.UnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisSpecialConstructionManager {

    /**
     * Some buildings like Zerg SUnken Colony need special treatment.
     */
    protected static boolean handledAsSpecialBuilding(UnitType building, ProductionOrder order) {
        if (building.equals(UnitType.Zerg_Sunken_Colony)) {
            ZergCreepColony.creepOneIntoSunkenColony();
            return true;
        }
        
        else if (building.isType(UnitType.Zerg_Lair)) {
            morphFromZergBuildingInto(UnitType.Zerg_Hatchery, UnitType.Zerg_Lair);
            return true;
        }
        
        else if (building.isType(UnitType.Zerg_Hive)) {
            morphFromZergBuildingInto(UnitType.Zerg_Lair, UnitType.Zerg_Hive);
            return true;
        }
        
        else if (building.isType(UnitType.Zerg_Greater_Spire)) {
            morphFromZergBuildingInto(UnitType.Zerg_Spire, UnitType.Zerg_Greater_Spire);
            return true;
        }
        
        return false;
    }
    
    // =========================================================

    private static void morphFromZergBuildingInto(UnitType from, UnitType into) {
        Unit building = (Unit) Select.ourBuildings().ofType(from).first();
        if (building == null) {
            System.err.println("No " + from + " found to morph into " + into);
        }
        else {
            building.morph(into);
        }
    }
    
}