package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;


public class TerranInfantry {

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
        
        if (nearestBunker != null && nearestBunker.distTo(unit) < maxDistanceToLoad) {
            unit.load(nearestBunker);
            unit.setTooltip("GTFInto bunker!");
            return true;
        }
        
        return false;
    }
    
    // =========================================================

    private static AUnit defineBunkerToLoadTo(AUnit unit) {
        Selection bunkers = Select.ourBuildings().ofType(AUnitType.Terran_Bunker)
                .inRadius(15, unit).havingSpaceFree(unit.spaceRequired());
        AUnit bunker = bunkers.nearestTo(unit);
        if (bunker != null) {
            AUnit mainBase = Select.mainBase();
            
            // Select the most distance (according to main base) bunker
            if (Missions.isGlobalMissionDefend() && mainBase != null) {
                AUnit mostDistantBunker = bunkers
                        .units()
                        .sortByGroundDistTo(mainBase.position(), false)
                        .first();
                return mostDistantBunker;
            }
            else {
                return bunker;
            }
        }
        
        return null;
    }
    
}
