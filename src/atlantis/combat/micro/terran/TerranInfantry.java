package atlantis.combat.micro.terran;

import atlantis.combat.missions.Missions;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
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
        if (unit.cooldownRemaining() <= 3 || unit.hp() >= 26) {
            return false;
        }

        if (unit.enemiesNearby().canAttack(unit, 7).isNotEmpty()) {
            return false;
        }

        AUnit medic = Select.ourOfType(AUnitType.Terran_Medic).inRadius(8, unit).havingEnergy(30).nearestTo(unit);
        if (medic != null && medic.distToMoreThan(unit, 2)) {
            return unit.move(medic, Actions.MOVE_SPECIAL, "BeHealed", false);
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

        Selection enemies = unit.enemiesNearby().inRadius(9, unit);

        if (
                enemies.atLeast(Enemy.zerg() ? 3 : 2)
        ) {
            if (unit.lastActionMoreThanAgo(5, Actions.USING_TECH)) {
                if (Select.ourOfType(AUnitType.Terran_Medic).inRadius(5, unit).havingEnergy(40).atLeast(2)) {
                    unit.useTech(stim());
                }
            }
            return true;
        }

        return false;
    }

    private static boolean tryUnloadingFromBunkerIfNeeded(AUnit unit) {
        if (!unit.isLoaded()) {
            return false;
        }

        if (unit.hasTargetPosition() && unit.targetPositionAtLeastAway(12)) {
            Select.ourOfType(AUnitType.Terran_Bunker).inRadius(1, unit).first().unloadAll();
            unit.setTooltipTactical("Unload");
            unit.addLog("UnloadToMove");
            return true;
        }

//        if (Select.enemyRealUnits().inRadius(6, unit).isEmpty()) {
        if (
                unit.enemiesNearby().isEmpty()
                && unit.lastActionMoreThanAgo(15)
        ) {
            Select.ourOfType(AUnitType.Terran_Bunker).inRadius(0.5, unit).first().unloadAll();
            unit.setTooltipTactical("Unload");
            return true;
        }

        return false;
    }

    public static boolean tryLoadingInfantryIntoBunkerIfNeeded(AUnit unit) {
        
        // Only Terran infantry get inside
        if (unit.isLoaded() || !unit.isMarine()) {
            return false;
        }

        // Without enemies around, don't do anything
        if (unit.enemiesNearby().empty()) {
            return false;
        }
        
        // =========================================================
        
        AUnit nearestBunker = defineBunkerToLoadTo(unit);
        int maxDistanceToLoad = Missions.isGlobalMissionDefend() ? 12 : 7;

//        if (
//                unit.lastActionMoreThanAgo(20)
////                || Select.enemyRealUnits().inRadius(6, unit).atLeast(1)
//        ) {
            if (nearestBunker != null && nearestBunker.distTo(unit) < maxDistanceToLoad) {
                unit.load(nearestBunker);
                unit.setTooltipTactical("GetToDaChoppa");
                unit.addLog("GetToDaChoppa");
                return true;
            }
//        }

        return false;
    }
    
    // =========================================================

    private static AUnit defineBunkerToLoadTo(AUnit unit) {
        Selection bunkers = Select.ourBuildings().ofType(AUnitType.Terran_Bunker)
                .inRadius(15, unit).havingSpaceFree(unit.spaceRequired());
        AUnit bunker = bunkers.nearestTo(unit);

//        System.out.println("bunker = " + bunker);
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
