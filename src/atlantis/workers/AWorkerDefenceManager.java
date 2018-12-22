package atlantis.workers;

import atlantis.units.AUnit;
import atlantis.units.Select;
import java.util.Collection;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AWorkerDefenceManager {

    /**
     * Attack other workers, run from enemies etc.
     */
    public static boolean handleDefenceIfNeeded(AUnit worker) {
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
    private static boolean handleFightEnemyIfNeeded(AUnit worker) {
        
        // DESTROY ENEMY BUILDINGS that are being built close to main base.
        if (Select.ourBases().inRadius(20, worker).count() > 0) {
            for (AUnit enemyBuilding : Select.enemy().buildings()
                    .inRadius(20, worker).listUnits()) {

                // Attack enemy building
                worker.attackUnit(enemyBuilding);
                return true;
            }
        }
        
        // FIGHT AGAINST ENEMY WORKERS
        for (AUnit enemy : Select.enemy().inRadius(2, worker).listUnits()) {
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
    private static boolean handleRunFromEnemyIfNeeded(AUnit worker) {
        
        // Define list of all units that are in range of shot.
        Collection<AUnit> enemiesInRange = Select.enemy().inRadius(12, worker)
                .canAttack(worker).listUnits();
        
        // Run from every combat unit...
        for (AUnit enemy : enemiesInRange) {
            
            // Enemy is non-worker unit
            if (!enemy.isWorker()) {
                worker.runFrom(enemy);
                return true;
            }
        }
        
        // ...but run from enemy workers only if seriously wounded.
        for (AUnit enemy : enemiesInRange) {
            if (enemy.isWorker() && worker.getHP() < 11) {
                worker.runFrom(enemy);
                return true;
            }
        }
        
        return false;
    }
    
}
