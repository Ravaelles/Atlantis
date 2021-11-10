package atlantis.workers;

import atlantis.AGame;
import atlantis.production.constructing.AConstructionManager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
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
                    worker.setTooltip("ProtectBuilder");
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
        if (worker.isWounded() || worker.id() % 5 != 0) {
            return false;
        }

        AUnit wounded = Select.ourWorkers().wounded().inRadius(2, worker).nearestTo(worker);

        if (wounded != null && wounded.isWounded() && wounded.isAlive() && !wounded.isBuilder()) {
            if (!worker.isRepairing()) {
                worker.repair(wounded, "Buddy!");
            }
            if (wounded.distToLessThan(worker, 0.6)) {
                if (!wounded.isBuilder() && !wounded.isRepairing() || wounded.isMoving()) {
                    wounded.stop("BeRepaired");
                }
            }
            return true;
        }

        return false;
    }

    private static boolean handleFightEnemyCombatUnits(AUnit worker) {
        if (Count.workers() <= 12 || Select.our().inRadius(4, worker).atMost(2)) {
            return false;
        }

        // FIGHT against ZERGLINGS
        for (AUnit enemy : Select.enemies(AUnitType.Zerg_Zergling).inRadius(2, worker).listUnits()) {
            if (worker.hp() <= 21 && runToFarthestMineral(worker, enemy)) {
                worker.setTooltip("Aaargh!");
                return true;
            }
            worker.attackUnit(enemy);
            worker.setTooltip("ForMotherland!");
            return true;
        }

        // FIGHT against COMBAT UNITS
        List<AUnit> enemies = Select.enemy().combatUnits()
                .excludeTypes(
                        AUnitType.Zerg_Lurker,
                        AUnitType.Zerg_Ultralisk,
                        AUnitType.Protoss_Archon,
                        AUnitType.Protoss_Reaver,
                        AUnitType.Protoss_Zealot
                )
                .inRadius(1, worker).listUnits();
        for (AUnit enemy : enemies) {
            worker.attackUnit(enemy);
            worker.setTooltip("ForMotherland!");
            return true;
        }

        return false;
    }

    private static boolean handleEnemyWorkersNearby(AUnit worker) {
        Selection enemyWorkers = Select.enemy().workers().inRadius(1.3, worker);
        for (AUnit enemy : enemyWorkers.listUnits()) {
            worker.setTooltip("NastyFuckers!");
            worker.attackUnit(enemy);
            return true;
        }

        return false;
    }

    private static boolean handleEnemyBuildingsOffensive(AUnit worker) {
        for (AUnit enemyBuilding : Select.enemy().buildings().inRadius(20, worker).listUnits()) {
            worker.attackUnit(enemyBuilding);
            worker.setTooltip("Cheesy!");
            return true;
        }

        return false;
    }

    private static boolean runToFarthestMineral(AUnit worker, AUnit enemy) {
        AUnit mineral = Select.minerals().inRadius(10, enemy).mostDistantTo(enemy);
        if (mineral != null) {
            worker.gather(mineral);
            worker.setTooltip("DidntSignUpForThis");
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
