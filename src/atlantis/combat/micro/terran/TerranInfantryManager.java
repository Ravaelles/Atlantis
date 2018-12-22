package atlantis.combat.micro.terran;

import atlantis.combat.squad.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwapi.Position;

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
        Select<?> bunkers = Select.ourBuildings().ofType(AUnitType.Terran_Bunker)
                .inRadius(15, unit).havingSpaceFree(unit.getSpaceRequired());
        AUnit bunker = bunkers.nearestTo(unit);
        if (bunker != null) {
            AUnit mainBase = Select.mainBase();
            
            // Select the most distance (according to main base) bunker
            if (Missions.isGlobalMissionDefend() && mainBase != null) {
                AUnit mostDistantBunker = bunkers.units().sortByGroundDistanceTo(mainBase.getPosition(), false).first();
                if (mostDistantBunker != null) {
                    return mostDistantBunker;
                }
            }
            else {
                return bunker;
            }
        }
        
        return null;
    }
    
}
