package atlantis.units.workers;

import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructing.AConstructionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

import java.util.List;

public class AWorkerDefenceManager {

    /**
     * Attack other workers, run from enemies etc.
     */
    public static boolean handleDefenceIfNeeded(AUnit worker) {
        if (worker.isRepairing()) {
            return false;
        }

        if (worker.enemiesNear().combatUnits().isEmpty()) {
            return false;
        }

        if (runFromReaverFix(worker)) {
            return true;
        }

        if (handleFightEnemyIfNeeded(worker)) {
            return true;
        }

        if (AvoidEnemies.avoidEnemiesIfNeeded(worker)) {
            worker.addLog("WorkerAvoid");
            return true;
        }

        // Dynamic Near CSV repairing
        if (We.terran() && AGame.canAfford(20, 0) && handleRepairNear(worker)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean runFromReaverFix(AUnit worker) {
        AUnit reaver = worker.enemiesNear().ofType(AUnitType.Protoss_Reaver).nearestTo(worker);
        if (reaver != null && reaver.distToLessThan(worker, 10)) {
            worker.runningManager().runFrom(reaver, 5, Actions.RUN_ENEMY, true);
            worker.setTooltip("OhFuckReaver!", true);
            worker.addLog("OhFuckReaver!");
            return true;
        }

        return false;
    }

    /**
     * Sometimes workers need to fight.
     */
    private static boolean handleFightEnemyIfNeeded(AUnit worker) {
        if (shouldNotFight(worker)) {
            return false;
        }

        // DESTROY ENEMY BUILDINGS that are being built close to main base.
        if (handleEnemyBuildingsOffensive(worker)) {
            return true;
        }

        // FIGHT against enemy WORKERS, worker rushes etc
        if (handleEnemyWorkersNear(worker)) {
            return true;
        }

        if (We.terran() && handleProtectScvBusyConstructing(worker)) {
            return true;
        }

        // Fight against ZERGLINGS
        if (handleFightEnemyCombatUnits(worker)) {
            return true;
        }

        return false;
    }

    private static boolean handleProtectScvBusyConstructing(AUnit worker) {
        if (Count.ourCombatUnits() >= 1) {
            return false;
        }

        if (worker.id() % 6 != 0 || worker.hp() <= 13) {
            return false;
        }

        for (AUnit builder : AConstructionManager.builders()) {
            if (builder.hp() < 40 && builder.lastUnderAttackLessThanAgo(30 * 10)) {
                AUnit nearestPeskyEnemyWorker = Select.enemy().workers().inRadius(3, builder).nearestTo(worker);
                if (nearestPeskyEnemyWorker != null) {
                    worker.setTooltipTactical("ProtectBuilder");
                    return worker.attackUnit(nearestPeskyEnemyWorker);
                }
            }
        }

        return false;
    }

    private static boolean shouldNotFight(AUnit worker) {
//        if (worker.idIsOdd()) {
//            return true;
//        }

        if (A.supplyUsed() >= 40) {
            return true;
        }

        if (worker.enemiesNear().empty() || worker.enemiesNear().inRadius(4, worker).empty()) {
            return true;
        }

        if (Enemy.protoss() && worker.hp() <= 22) {
            return true;
        }
        else if (worker.hp() <= 18) {
            return true;
        }

        if (worker.isBuilder() || worker.isConstructing()) {
            return true;
        }

//        boolean isNearBase = Select.ourBases().inRadius(20, worker).atLeast(1);
//        if (!isNearBase) {
//            return true;
//        }

        return false;
    }

    private static boolean handleRepairNear(AUnit worker) {
        if (!worker.isWounded() || (worker.id() % 5 != 0 && !worker.isRepairing())) {
            return false;
        }

        AUnit wounded = Select.ourWorkers().wounded().inRadius(3, worker).nearestTo(worker);

        if (wounded != null && A.hasMinerals(5) && wounded.isWounded() && wounded.isAlive() && !wounded.isBuilder()) {
            worker.repair(wounded, "BuddyRepair!", true);
            wounded.repair(worker, "BuddyRepair!", true);
//            if (!worker.isRepairing()) {
//            }
//            if (!wounded.isRepairing()) {
//            }
//            if (wounded.distToLessThan(worker, 0.6)) {
//                if (!wounded.isBuilder() && !wounded.isRepairing() || wounded.isMoving()) {
//                    wounded.stop("BeRepaired");
//                }
//                wounded.setTooltip("BeRepaired");
//            }
            return true;
        }

        return false;
    }

    private static boolean handleFightEnemyCombatUnits(AUnit worker) {
        if (worker.hp() <= 20) {
            return false;
        }

        if (worker.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).isNotEmpty()) {
            return attackNearestEnemy(worker);
        }

        if (worker.distToMoreThan(Select.main(), 9)) {
            return false;
        }

//        if (Count.workers() <= 8 || Select.our().inRadius(4, worker).atMost(2)) {
        if (Count.workers() <= 8) {
            return false;
        }

        if (Select.enemyCombatUnits().ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Zerg_Lurker,
                AUnitType.Zerg_Ultralisk,
                AUnitType.Protoss_Archon,
                AUnitType.Protoss_Dark_Templar,
                AUnitType.Protoss_Reaver
//                AUnitType.Protoss_Zealot
        ).inRadius(8, worker).count() >= 1) {
            return false;
        }

        // FIGHT against ZERGLINGS
        for (AUnit enemy : Select.enemies(AUnitType.Zerg_Zergling).inRadius(2, worker).list()) {
//            if ((worker.hp() <= 20 || Count.workers() <= 9) && runToFarthestMineral(worker, enemy)) {
            if (worker.hp() <= 20) {
                worker.setTooltipTactical("Aaargh!");
                worker.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, true);
                return true;
            }
            worker.attackUnit(enemy);
            worker.setTooltipTactical("ForMotherland!");
            return true;
        }

        // FIGHT against COMBAT UNITS
        List<AUnit> enemies = worker.enemiesNear()
                .canBeAttackedBy(worker, 2)
                .list();
        for (AUnit enemy : enemies) {
            if (worker.hp() <= 20) {
                worker.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, false);
                return true;
            }
            worker.setTooltipTactical("FurMotherland!");
            return worker.attackUnit(enemy);
        }

        return false;
    }

    private static boolean attackNearestEnemy(AUnit worker) {
        AUnit enemy = worker.enemiesNear().canBeAttackedBy(worker, 8).nearestTo(worker);
        if (enemy == null) {
            return false;
        }

        worker.setTooltip("Protect", true);
        worker.attackUnit(enemy);
        return true;
    }

    private static boolean handleEnemyWorkersNear(AUnit worker) {
        Selection enemyWorkers = Select.enemy().workers().inRadius(1.3, worker);
        for (AUnit enemy : enemyWorkers.list()) {
            worker.setTooltipTactical("NastyFuckers!");
            worker.attackUnit(enemy);
            return true;
        }

        return false;
    }

    private static boolean handleEnemyBuildingsOffensive(AUnit worker) {
        if (A.isUms() || (A.seconds() >= 40 && Count.ourCombatUnits() >= 6)) {
            return false;
        }

        for (AUnit enemyBuilding : Select.enemy().buildings().inRadius(20, worker).list()) {
            worker.attackUnit(enemyBuilding);
            worker.setTooltipTactical("Cheesy!");
            return true;
        }

        return false;
    }

    private static boolean runToFarthestMineral(AUnit worker, AUnit enemy) {
        AUnit mineral = Select.minerals().inRadius(10, enemy).mostDistantTo(enemy);
        if (mineral != null) {
            worker.gather(mineral);
            worker.setTooltipTactical("DidntSignUpForThis");
            return true;
        }
        return false;
    }

    /**
     * If unit is overwhelmed, low on health etc, just run from enemy.
     */
//    private static boolean handleRunFromEnemyIfNeeded(AUnit worker) {
//
//        // Define list of all units that are in range of shot.
//        Collection<AUnit> enemiesInRange = Select.enemyRealUnits().inRadius(12, worker)
//                .canShootAt(worker, 1 + worker.woundPercent() / 40).listUnits();
//
//        // Run from every combat unit...
//        for (AUnit enemy : enemiesInRange) {
//            if (!enemy.isWorker()) {
//                worker.runningManager().runFrom(enemy, 2);
//                return true;
//            }
//        }
//
//        // ...but run from enemy workers only if seriously wounded.
//        for (AUnit enemy : enemiesInRange) {
//            if (enemy.isWorker() && worker.hp() < 11) {
//                worker.runningManager().runFrom(enemy, 2);
//                return true;
//            }
//        }
//
//        return false;
//    }
    
}
