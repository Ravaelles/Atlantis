package atlantis.production.constructing;

import atlantis.combat.micro.zerg.ZergCreepColony;
import atlantis.game.AGame;
import atlantis.production.ProductionOrder;
import atlantis.production.constructing.position.TerranAddonManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;


public class SpecificConstructionRequests {

    /**
     * Some buildings like Zerg SUnken Colony need special treatment.
     */
    protected static boolean handledAsSpecialBuilding(AUnitType building, ProductionOrder order) {
        if (handledTerranSpecialBuilding(building, order)) {
            return true;
        }
        return handledZergSpecialBuilding(building, order);
    }

    // === Terran ========================================    

    private static boolean handledTerranSpecialBuilding(AUnitType building, ProductionOrder order) {
        if (!AGame.isPlayingAsTerran()) {
            return false;
        }
        
        if (building.isAddon()) {
            TerranAddonManager.buildNewAddon(building, order);
            return true;
        }
        
        return false;
    }
    
    // === Zerg ========================================

    private static boolean handledZergSpecialBuilding(AUnitType building, ProductionOrder order) {
        if (!AGame.isPlayingAsZerg()) {
            return false;
        }
        
        if (building.equals(AUnitType.Zerg_Sunken_Colony)) {
            ZergCreepColony.creepOneIntoSunkenColony();
            return true;
        }
        
        else if (building.is(AUnitType.Zerg_Lair)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Hatchery, AUnitType.Zerg_Lair);
            return true;
        }
        
        else if (building.is(AUnitType.Zerg_Hive)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Lair, AUnitType.Zerg_Hive);
            return true;
        }
        
        else if (building.is(AUnitType.Zerg_Greater_Spire)) {
            morphFromZergBuildingInto(AUnitType.Zerg_Spire, AUnitType.Zerg_Greater_Spire);
            return true;
        }
        
        return false;
    }
    
    private static void morphFromZergBuildingInto(AUnitType from, AUnitType into) {
        AUnit building = Select.ourBuildings().ofType(from).first();
        if (building == null) {
            System.err.println("No " + from + " found to morph into " + into);
        }
        else {
            building.morph(into);
        }
    }
    
}