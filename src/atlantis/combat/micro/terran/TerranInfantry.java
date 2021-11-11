package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.UnitAction;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.wrappers.ATech;
import bwapi.TechType;


public class TerranInfantry {

    public static boolean update(AUnit unit) {
        if (stimpack(unit)) {
            return true;
        }

        if (tryUnloadingFromBunkerIfNeeded(unit)) {
            return true;
        }

        if (tryLoadingInfantryIntoBunkerIfNeeded(unit)) {
            return true;
        }
        
        if (goToNearMedic(unit)) {
            return true;
        }

        return false;
//        return tryLoadingInfantryIntoBunkerIfPossible(unit);
    }

    // =========================================================

    private static boolean goToNearMedic(AUnit unit) {
        if (!unit.isWounded()) {
            return false;
        }

        if (Select.enemyCombatUnits().inRadius(8, unit).isNotEmpty()) {
            return false;
        }

        AUnit medic = Select.ourOfType(AUnitType.Terran_Medic).inRadius(8, unit).havingEnergy(30).nearestTo(unit);
        if (medic != null) {
            return unit.move(medic, UnitActions.MOVE, "BeHealed");
        }

        return false;
    }

    private static boolean stimpack(AUnit unit) {
        if (!ATech.isResearched(stim()) || !unit.isMarine()) {
            return false;
        }

        if (unit.hp() <= 20 || unit.isStimmed()) {
            return false;
        }

        Selection enemies = Select.enemyRealUnits().inRadius(9, unit);

        if (
                enemies.atLeast(Enemy.zerg() ? 2 : 1)
        ) {
            if (unit.lastActionMoreThanAgo(5, UnitActions.USING_TECH)) {
                unit.useTech(stim());
//                System.out.println("------------ STIM! " + unit.idWithHash() + " @ " + A.now());
            }
            return true;
        }

        return false;
    }

    private static boolean tryUnloadingFromBunkerIfNeeded(AUnit unit) {
        if (!unit.isLoaded()) {
            return false;
        }

//        if (Select.enemyRealUnits().inRadius(6, unit).isEmpty()) {
        if (
                unit.lastActionLessThanAgo(1, UnitActions.MOVE)
                && Select.enemyRealUnits().inRadius(6, unit).isEmpty()
        ) {
            Select.ourOfType(AUnitType.Terran_Bunker).inRadius(0.5, unit).first().unloadAll();
            unit.setTooltip("Unload");
            return true;
        }

        return false;
    }

    public static boolean tryLoadingInfantryIntoBunkerIfNeeded(AUnit unit) {
        
        // Only Terran infantry get inside
        if (unit.isLoaded() || !unit.isMarine()) {
            return false;
        }
        
        // =========================================================
        
        AUnit nearestBunker = defineBunkerToLoadTo(unit);
        int maxDistanceToLoad = Missions.isGlobalMissionDefend() ? 15 : 9;

        if (
                unit.lastActionMoreThanAgo(10, UnitActions.MOVE)
//                || Select.enemyRealUnits().inRadius(6, unit).atLeast(1)
        ) {
            if (nearestBunker != null && nearestBunker.distTo(unit) < maxDistanceToLoad) {
                unit.load(nearestBunker);
                unit.setTooltip("GTFInto bunker!");
                return true;
            }
        }

        return false;
    }
    
    // =========================================================

    private static AUnit defineBunkerToLoadTo(AUnit unit) {
        Selection bunkers = Select.ourBuildings().ofType(AUnitType.Terran_Bunker)
                .inRadius(15, unit).havingSpaceFree(unit.spaceRequired());
        AUnit bunker = bunkers.nearestTo(unit);
//        if (bunker != null) {
//            AUnit mainBase = Select.main();
//
//            // Select the most distance (according to main base) bunker
//            if (Missions.isGlobalMissionDefend() && mainBase != null) {
//                AUnit mostDistantBunker = bunkers
//                        .units()
//                        .sortByGroundDistTo(mainBase.position(), false)
//                        .first();
//                return mostDistantBunker;
//            }
//            else {
//                return bunker;
//            }
//        }
        
        return bunker;
    }

    private static TechType stim() {
        return TechType.Stim_Packs;
    }
    
}
