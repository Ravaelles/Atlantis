package atlantis.combat.squad.missions;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class TerranInfantryManager {
    
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
