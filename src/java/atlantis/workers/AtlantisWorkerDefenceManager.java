package atlantis.workers;

import atlantis.wrappers.Select;
import bwapi.Unit;
import java.util.Collection;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisWorkerDefenceManager {

    /**
     * Attack other workers, run from enemies etc.
     */
    public static boolean handleDefenceIfNeeded(Unit worker) {
        if (handleFightEnemyIfNeeded(worker)) {
            return true;
        }
        
        if (handleRunFromEnemyIfNeeded(worker)) {
            return true;
        }
        
        return false;
    }
    
    // =========================================================

    /**
     * Sometimes workers need to fight.
     */
    private static boolean handleFightEnemyIfNeeded(Unit worker) {
        
        // DESTROY ENEMY BUILDINGS that are being built close to main base.
        if (Select.ourBases().inRadius(20, worker).count() > 0) {
            for (Unit enemyBuilding : Select.enemy().buildings().inRadius(20, worker).listUnits()) {

                // Attack enemy building
                worker.attackUnit(enemyBuilding);
                return true;
            }
        }
        
        // FIGHT AGAINST ENEMY WORKERS
        for (Unit enemy : Select.enemy().inRadius(2, worker).listUnits()) {
            if (enemy.isWorker() && worker.getHP() >= 11) {
                worker.attackUnit(enemy);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * If unit is overwhelmed, low on health etc, just run from enemy.
     */
    private static boolean handleRunFromEnemyIfNeeded(Unit worker) {
        
        // Define list of all units that are in range of shot.
        Collection<Unit> enemiesInRange = Select.enemy().inRadius(12, worker).thatCanShoot(worker).listUnits();
        
        // Run from every combat unit...
        for (Unit enemy : enemiesInRange) {
            
            // Enemy is non-worker unit
            if (!enemy.isWorker()) {
                worker.runFrom(enemy);
                return true;
            }
        }
        
        // ...but run from enemy workers only if seriously wounded.
        for (Unit enemy : enemiesInRange) {
            if (enemy.isWorker() && worker.getHP() < 11) {
                worker.runFrom(enemy);
                return true;
            }
        }
        
        return false;
    }
    
}
