package atlantis.workers;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class AWorkerDefenceManager {

    /**
     * Attack other workers, run from enemies etc.
     */
    public static boolean handleDefenceIfNeeded(AUnit worker) {
        if (worker.hp() <= 11) {
            return false;
        }

        if (handleFightEnemyIfNeeded(worker)) {
            return true;
        }

        return false;
//        return handleRunFromEnemyIfNeeded(worker);
    }
    
    // =========================================================

    /**
     * Sometimes workers need to fight.
     */
    private static boolean handleFightEnemyIfNeeded(AUnit worker) {
        
        // DESTROY ENEMY BUILDINGS that are being built close to main base.
        if (Select.ourBases().inRadius(20, worker).count() > 0) {
            for (AUnit enemyBuilding : Select.enemy().buildings().inRadius(20, worker).listUnits()) {
                System.out.println("enemyBuilding = " + enemyBuilding);

                // Attack enemy building
                worker.attackUnit(enemyBuilding);
                worker.setTooltip("Cheesy!");
                return true;
            }
        }

        // FIGHT against enemy WORKERS
        for (AUnit enemy : Select.enemy().inRadius(2, worker).listUnits()) {
            if (enemy.isWorker() && worker.hp() >= 11) {
                System.out.println("enemy = " + enemy);
                worker.setTooltip("NastyFuckers!");
                worker.attackUnit(enemy);
                return true;
            }
        }

        // FIGHT against COMBAT UNITS
        for (AUnit enemy : Select.enemy().combatUnits().inRadius(1, worker).listUnits()) {
            System.out.println("enemy combat = " + enemy);
            worker.attackUnit(enemy);
            worker.setTooltip("ForMotherland!");
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
