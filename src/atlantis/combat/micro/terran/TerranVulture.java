package atlantis.combat.micro.terran;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.wrappers.ATech;
import bwapi.TechType;


public class TerranVulture {

    public static boolean update(AUnit unit) {
        return handlePlantMines(unit);
    }
    
    // =========================================================

    private static boolean handlePlantMines(AUnit unit) {

        // Unit gets status "stuck" after mine has been planted, being the only way I know of to
        // define that a mine planting has been finished.
//        if (unit.isUnitAction(UnitActions.USING_TECH) && (unit.isStuck() || unit.isIdle() || !unit.isMoving())) {
        if (unit.isUnitAction(UnitActions.USING_TECH) && unit.getLastOrderFramesAgo() > 15) {
            unit.setUnitAction(null);
            unit.setTooltip("Planted!");
            return false;
        }

        // Can't allow to interrupt
        if (unit.isUnitAction(UnitActions.USING_TECH)) {
            unit.setTooltip("Planting mine");
            return true;
        }

        // If out of mines or mines ain't researched, don't do anything.
        if (unit.getMinesCount() <= 0 || !ATech.isResearched(TechType.Spider_Mines)) {
            return false;
        }

        // Disallow mines close to buildings
        AUnit nearestBuilding = Select.ourBuildings().nearestTo(unit);
        if (nearestBuilding != null && nearestBuilding.distTo(unit) <= 7) {
            unit.setTooltip("Don't mine");
            return false;
        }
        
        // If enemies are too close don't do it
        if (Select.enemyRealUnits().inRadius(6, unit).count() > 0) {
            return false;
        }
        
        // If too many our units around, don't mine
        if (Select.ourCombatUnits().inRadius(7, unit).count() >= 4) {
            return false;
        }
        
        // Don't cluster mines too much
        Select<?> nearbyMines = Select.ourOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(8, unit);
        if ((nearbyMines.count() <= 3 || (unit.getMinesCount() >= 3 && nearbyMines.count() <= 4)) 
                && nearbyMines.inRadius(1, unit).count() == 0) {
            unit.useTech(TechType.Spider_Mines, unit.getPosition());
            unit.setUnitAction(UnitActions.USING_TECH);
            unit.setTooltip("Plant mine");
            return true;
        }
        
        return false;
    }
    
}
