package atlantis.combat.micro.terran.bunker;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class UnloadFromBunkers extends Manager {

    public UnloadFromBunkers(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[] {
            PreventMaginotLine.class,
        };
    }

    @Override
    public Manager handle() {
        if (tryUnloadingFromBunkerIfNeeded()) {
            return usedManager(this);
        }

        return handleSubmanagers();
    }

    public boolean tryUnloadingFromBunkerIfNeeded() {
        if (!unit.isLoaded()) {
            return false;
        }

        if (unit.lastActionLessThanAgo(30 * 2, Actions.LOAD)) {
            return false;
        }

        if (unit.enemiesNearInRadius(3) > 0) {
            return false;
        }

//        if (preventEnemiesFromAttackingNearBuildingsWithoutConsequences()) {
//            return true;
//        }

        if (
//            preventMaginotLine.preventFromActingLikeFrenchOnMaginotLine(unit)
                tooFarToAttackTheTargetFromBunker()
                || noEnemiesNear()
        ) {
            return unloadFromBunker();
        }

        return false;
    }

    private boolean noEnemiesNear() {
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

    private boolean tooFarToAttackTheTargetFromBunker() {
        if (unit.hasTargetPosition()
            && unit.targetPositionAtLeastAway(6.05)
            && unit.enemiesNear().inRadius(4, unit).empty()
        ) {
            unit.setTooltipTactical("UnloadToMove");
            unit.addLog("UnloadToMove");
            return true;
        }

        return false;
    }

    // =========================================================


    private boolean preventEnemiesFromAttackingWorkersWithoutConsequences() {
        Selection enemiesAttacking = unit.enemiesNear().havingAnyTarget();
        for (AUnit enemy : enemiesAttacking.list()) {
            AUnit enemyTarget = enemy.target();
            if (
                enemyTarget != null
                    && enemyTarget.isWorker()
                    && enemyTarget.distToMoreThan(unit, 6)
                    && unit.distTo(enemy) >= 3
            ) {
                unit.setTooltipTactical("SupportWorkers");
                return true;
            }
        }

        return false;
    }

    private boolean preventEnemiesFromAttackingNearBuildingsWithoutConsequences() {
        Selection enemiesAttackingBuildings = unit.enemiesNear().havingTargetedBuildings();
        for (AUnit enemy : enemiesAttackingBuildings.list()) {
            AUnit enemyTarget = enemy.target();
            if (!enemyTarget.isBunker()) {
//                if (enemyTarget.woundPercent() >= 2 || enemiesAttackingBuildings.atMost(3)) {
                if (enemyTarget.woundPercent() >= 2 || unit.idIsEven()) {
                    if (!enemy.isHydralisk() || unit.distTo(enemy) > 7) {
                        //                    System.err.println("SupportDemBuildings " + enemyTarget);
                        unit.setTooltipTactical("SupportDemBuildings");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean unloadFromBunker() {
        unit.loadedInto().addLog("UnloadCrew");
        unit.loadedInto().unloadAll();
        return true;
//        Select.ourOfType(AUnitType.Terran_Bunker).inRadius(0.5, unit).first().unloadAll();
    }
}
