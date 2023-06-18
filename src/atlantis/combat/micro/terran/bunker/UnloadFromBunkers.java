package atlantis.combat.micro.terran.bunker;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class UnloadFromBunkers {

    public static boolean tryUnloadingFromBunkerIfNeeded(AUnit unit) {
        if (!unit.isLoaded()) {
            return false;
        }

        if (unit.lastActionLessThanAgo(30 * 4, Actions.LOAD)) {
            return false;
        }

//        if (preventEnemiesFromAttackingNearBuildingsWithoutConsequences(unit)) {
//            return true;
//        }

        if (
            preventFromActingLikeFrenchOnMaginotLine(unit)
            || tooFarToAttackTheTargetFromBunker(unit)
            || noEnemiesNear(unit)
        ) {
            return unloadFromBunker(unit);
        }

        return false;
    }

    private static boolean noEnemiesNear(AUnit unit) {
        AUnit bunker = unit.loadedInto();
//        if (Select.enemyRealUnits().inRadius(6, unit).isEmpty()) {
        if (
            unit.enemiesNear().inRadius(8, bunker).isEmpty()
        ) {
            if (unit.lastActionLessThanAgo(5, Actions.UNLOAD)) {
                unit.setTooltip("Unloading");
                return true;
            }

            if (!unit.isMissionDefendOrSparta() || unit.distToFocusPoint() >= 8) {
                unit.setTooltipTactical("Unload");
                return true;
            }
        }

        return false;
    }

    private static boolean tooFarToAttackTheTargetFromBunker(AUnit unit) {
        if (unit.hasTargetPosition()
            && unit.targetPositionAtLeastAway(6.1)
            && unit.enemiesNear().inRadius(4, unit).empty()
        ) {
            unit.setTooltipTactical("UnloadToMove");
            unit.addLog("UnloadToMove");
            return true;
        }

        return false;
    }

    // =========================================================

    public static boolean preventFromActingLikeFrenchOnMaginotLine(AUnit unit) {
        if (!unit.isLoaded()) {
            return false;
        }

        if (unit.hpLessThan(22)) {
            return false;
        }

        if (preventEnemiesFromAttackingNearBuildingsWithoutConsequences(unit)) {
            return true;
        }

        if (preventEnemiesFromAttackingWorkersWithoutConsequences(unit)) {
            return true;
        }

        return false;
    }

    private static boolean preventEnemiesFromAttackingWorkersWithoutConsequences(AUnit unit) {
        Selection enemiesAttacking = unit.enemiesNear().havingAnyTarget();
        for (AUnit enemy : enemiesAttacking.list()){
            AUnit enemyTarget = enemy.target();
            if (enemyTarget != null && enemyTarget.isWorker() && enemyTarget.distToMoreThan(unit, 6)) {
                unit.setTooltipTactical("SupportWorkers");
                return true;
            }
        }

        return false;
    }

    private static boolean preventEnemiesFromAttackingNearBuildingsWithoutConsequences(AUnit unit) {
        Selection enemiesAttackingBuildings = unit.enemiesNear().havingTargetedBuildings();
        for (AUnit enemy : enemiesAttackingBuildings.list()){
            AUnit enemyTarget = enemy.target();
            if (!enemyTarget.isBunker()) {
//                if (enemyTarget.woundPercent() >= 2 || enemiesAttackingBuildings.atMost(3)) {
                if (enemyTarget.woundPercent() >= 2 || unit.idIsEven()) {
                    System.err.println("SupportDemBuildings " + enemyTarget);
                    unit.setTooltipTactical("SupportDemBuildings");
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean unloadFromBunker(AUnit unit) {
        unit.loadedInto().addLog("UnloadCrew");
        unit.loadedInto().unloadAll();
        return true;
//        Select.ourOfType(AUnitType.Terran_Bunker).inRadius(0.5, unit).first().unloadAll();
    }
}
