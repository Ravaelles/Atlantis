package atlantis.combat.micro.terran;

import atlantis.combat.squad.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class TerranInfantryManager {

    public static boolean update(AUnit unit) {
        return tryLoadingInfantryIntoBunkerIfPossible(unit);
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
        
        AUnit nearestBunker = defineBunkerToLoadTo(unit);
        int maxDistanceToLoad = Missions.isGlobalMissionDefend() ? 15 : 6;
        
        if (nearestBunker != null && nearestBunker.distanceTo(unit) < maxDistanceToLoad) {
            unit.load(nearestBunker);
            unit.setTooltip("GTFInto bunker!");
            return true;
        }
        
        return false;
    }
    
    // =========================================================

    private static AUnit defineBunkerToLoadTo(AUnit unit) {
        AUnit bunker = Select.ourBuildings().ofType(AUnitType.Terran_Bunker)
                .havingSpaceFree(unit.getSpaceRequired()).nearestTo(unit);
        if (bunker != null) {
            
            // Select the most distance (according to main base) bunker
            if (Missions.isGlobalMissionDefend()) {
                
            }
            else {
                return bunker;
            }
        }
        
        return null;
    }
    
}
