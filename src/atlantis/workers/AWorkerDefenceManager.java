package atlantis.workers;

import atlantis.AGame;
import atlantis.production.constructing.AConstructionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.A;
import atlantis.util.We;

import java.util.List;

public class AWorkerDefenceManager {

    /**
     * Attack other workers, run from enemies etc.
     */
    public static boolean handleDefenceIfNeeded(AUnit worker) {
        if (shouldNotFight(worker)) {
            return false;
        }

        if (handleFightEnemyIfNeeded(worker)) {
            return true;
        }

        // Dynamic nearby CSV repairing
        if (We.terran() && AGame.canAfford(20, 0) && handleRepairNearby(worker)) {
            return true;
        }

        return false;
    }

    // =========================================================

    /**
     * Sometimes workers need to fight.
     */
    private static boolean handleFightEnemyIfNeeded(AUnit worker) {

        // DESTROY ENEMY BUILDINGS that are being built close to main base.
        if (handleEnemyBuildingsOffensive(worker)) {
            return true;
        }

        // FIGHT against enemy WORKERS, worker rushes etc
        if (handleEnemyWorkersNearby(worker)) {
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
        if (worker.isBuilder() || worker.isConstructing()) {
            return true;
        }

        if (worker.hp() <= 18) {
            return true;
        }

        boolean isNearBase = Select.ourBases().inRadius(20, worker).atLeast(1);
        if (!isNearBase) {
            return true;
        }

        return false;
    }

    private static boolean handleRepairNearby(AUnit worker) {
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
        if (Count.workers() <= 12 || Select.our().inRadius(4, worker).atMost(2)) {
            return false;
        }

        if (Select.enemyCombatUnits().ofType(
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Zerg_Lurker,
                AUnitType.Zerg_Ultralisk,
                AUnitType.Protoss_Archon,
                AUnitType.Protoss_Reaver,
                AUnitType.Protoss_Zealot
        ).inRadius(8, worker).isNotEmpty()) {
            return false;
        }

        // FIGHT against ZERGLINGS
        for (AUnit enemy : Select.enemies(AUnitType.Zerg_Zergling).inRadius(2, worker).list()) {
            if ((worker.hp() <= 24 || Count.workers() <= 9) && runToFarthestMineral(worker, enemy)) {
                worker.setTooltipTactical("Aaargh!");
                return true;
            }
            worker.attackUnit(enemy);
            worker.setTooltipTactical("ForMotherland!");
            return true;
        }

        // FIGHT against COMBAT UNITS
        List<AUnit> enemies = Select.enemyCombatUnits()
                .inRadius(1, worker)
                .canBeAttackedBy(worker, 1)
                .list();
        for (AUnit enemy : enemies) {
            worker.attackUnit(enemy);
            worker.setTooltipTactical("FurMotherland!");
            return true;
        }

        return false;
    }

    private static boolean handleEnemyWorkersNearby(AUnit worker) {
        Selection enemyWorkers = Select.enemy().workers().inRadius(1.3, worker);
        for (AUnit enemy : enemyWorkers.list()) {
            worker.setTooltipTactical("NastyFuckers!");
            worker.attackUnit(enemy);
            return true;
        }

        return false;
    }

    private static boolean handleEnemyBuildingsOffensive(AUnit worker) {
        if (A.isUms() || A.seconds() <= 40) {
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
