package atlantis.combat.micro.terran;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranInfantryManager {

    public static boolean update(AUnit unit) {
        tryLoadingInfantryIntoBunkerIfPossible(unit);
    }
    
    /**
     *
     */
    public static boolean tryLoadingInfantryIntoBunkerIfPossible(AUnit unit) {
        
        // Only Terran infantry get inside
        if (!unit.type().isTerranInfantry() || unit.type().isMedic() || unit.isLoaded()) {
            return false;
        }
        
        // =========================================================
        
        AUnit nearestBunker = Select.ourBuildings().ofType(AUnitType.Terran_Bunker).nearestTo(unit);
        if (nearestBunker != null && nearestBunker.distanceTo(unit) < 15) {
            unit.load(nearestBunker);
            return true;
        }
        
        return false;
    }
    
}
