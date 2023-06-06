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
        if (unit.isHealthy() && unit.distToSquadCenter() <= 6) {
            return false;
        }

        if (unit.cooldownRemaining() <= 3 || unit.hp() >= 26) {
            return false;
        }

//        if (unit.enemiesNear().canAttack(unit, 7).isNotEmpty()) {
//            return false;
//        }

        AUnit medic = Select.ourOfType(AUnitType.Terran_Medic).inRadius(8, unit).havingEnergy(25).nearestTo(unit);
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

        Selection enemies = unit.enemiesNear().inRadius(9, unit);

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

        if (Enemy.protoss() && unit.hp() >= 40 && unit.id() % 3 == 0) {
            return true;
        }

        return false;
    }

    private static boolean tryUnloadingFromBunkerIfNeeded(AUnit unit) {
        if (!unit.isLoaded()) {
            return false;
        }

        if (
            unit.hasTargetPosition()
                && unit.targetPositionAtLeastAway(6.1)
                && unit.enemiesNear().inRadius(4, unit).empty()
        ) {
            unit.setTooltipTactical("Unload");
            unit.addLog("UnloadToMove");
            return unloadFromBunker(unit);
        }

//        if (Select.enemyRealUnits().inRadius(6, unit).isEmpty()) {
        if (
            unit.enemiesNear().isEmpty()
                && unit.lastActionMoreThanAgo(15)
        ) {
            if (!unit.isMissionDefendOrSparta() || unit.distToFocusPoint() >= 10) {
                unit.setTooltipTactical("Unload");
                return unloadFromBunker(unit);
            }
        }

        return false;
    }

    public static boolean tryLoadingInfantryIntoBunkerIfNeeded(AUnit unit) {
        if (unit.lastActionLessThanAgo(10, Actions.LOAD)) {
            unit.addLog("Loading");
            return true;
        }

        // Only Terran infantry get inside
        if (unit.isLoaded() || (!unit.isMarine() && !unit.isGhost())) {
            return false;
        }

        // Without enemies around, don't do anything
        Selection enemiesNear = unit.enemiesNear().canAttack(unit, 15);
        if (enemiesNear.excludeMedics().empty()) {
            return false;
        }

        // =========================================================

        AUnit nearestBunker = defineBunkerToLoadTo(unit);
        double maxDistanceToLoad = Missions.isGlobalMissionDefend() ? 5.2 : 8.2;

        if (
            nearestBunker != null
                && nearestBunker.hasFreeSpaceFor(unit)
                && nearestBunker.distTo(unit) < maxDistanceToLoad
                && (
                    nearestBunker.spaceRemaining() >= 2
                    || (
                        enemiesNear.inRadius(1.6, unit).atMost(1)
                        && (!enemiesNear.onlyMelee() || unit.nearestEnemyDist() < 5)
                    )
                )
        ) {
            unit.load(nearestBunker);

            String t = "GetToDaChoppa";
            unit.setTooltipTactical(t);
            unit.addLog(t);
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean unloadFromBunker(AUnit unit) {
        unit.loadedInto().addLog("UnloadCrew");
        unit.loadedInto().unloadAll();
        return true;
//        Select.ourOfType(AUnitType.Terran_Bunker).inRadius(0.5, unit).first().unloadAll();
    }

    // =========================================================

    private static AUnit defineBunkerToLoadTo(AUnit unit) {
        return Select.ourOfType(AUnitType.Terran_Bunker)
            .inRadius(15, unit)
            .havingSpaceFree(unit.spaceRequired())
            .nearestTo(unit);

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

//        return bunker;
    }

    private static TechType stim() {
        return TechType.Stim_Packs;
    }

}
