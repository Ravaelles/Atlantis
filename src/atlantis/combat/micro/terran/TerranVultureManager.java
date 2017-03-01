package atlantis.combat.micro.terran;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.wrappers.ATech;
import bwapi.TechType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranVultureManager {

    public static boolean update(AUnit unit) {
        unit.removeTooltip();
        
        if (handlePlantMines(unit)) {
            return true;
        }
        
        return false;
    }
    
    // =========================================================

    private static boolean handlePlantMines(AUnit unit) {
        if (unit.getMinesCount() <= 0 || !ATech.isResearched(TechType.Spider_Mines) || unit.isUnitAction(UnitActions.USING_TECH)) {
            return false;
        }
        
        if (unit.isAttackFrame()) {
//        if (unit.isAttacking() && unit.getGroundWeaponCooldown() <= 0) {
            return false;
        }
        
        // Disallow mines close to buildings
        AUnit nearestBuilding = Select.ourBuildings().nearestTo(unit);
        if (nearestBuilding != null && nearestBuilding.distanceTo(unit) < 8) {
            unit.setTooltip("Don't mine");
            return false;
        }
        
        // If enemies are too close don't do it
        if (Select.enemyRealUnits().inRadius(4, unit).count() > 0) {
            return false;
        }
        
        // Don't cluster mines too much
        Select<?> nearbyMines = Select.ourOfType(AUnitType.Terran_Vulture_Spider_Mine).inRadius(8, unit);
        if ((nearbyMines.count() <= 3 || (unit.getMinesCount() >= 3 && nearbyMines.count() <= 4)) 
                && nearbyMines.inRadius(1, unit).count() == 0) {
            unit.useTech(TechType.Spider_Mines, unit.getPosition());
            unit.setTooltip("Planting mine");
            return true;
        }
        
        return false;
    }
    
}
