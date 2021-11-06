package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.A;
import atlantis.wrappers.ATech;
import bwapi.TechType;
import bwapi.UpgradeType;


public class TerranInfantry {

    public static boolean update(AUnit unit) {
        if (handleStimpack(unit)) {
            return true;
        }

        return false;
//        return tryLoadingInfantryIntoBunkerIfPossible(unit);
    }

    // =========================================================

    private static boolean handleStimpack(AUnit unit) {
        if (!ATech.isResearched(stim()) || !unit.is(AUnitType.Terran_Marine)) {
            return false;
        }

        if (unit.hp() <= 20 || unit.isStimmed()) {
            return false;
        }

        Selection enemies = Select.enemyRealUnits().inRadius(8, unit);

        if (
                enemies.clone().ofType(AUnitType.Zerg_Lurker).atLeast(1)
                || enemies.clone().atLeast(4)
        ) {
            if (unit.lastActionMoreThanAgo(5, UnitActions.USING_TECH)) {
                unit.useTech(stim());
            }
            System.out.println("STIM! " + unit.idWithHash() + " @ " + A.now());
            return true;
        }

        return false;
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

    private static TechType stim() {
        return TechType.Stim_Packs;
    }
    
}
