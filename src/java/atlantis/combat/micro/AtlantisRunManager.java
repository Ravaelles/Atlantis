package atlantis.combat.micro;

import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisRunManager {
    
    /**
     * Makes unit run (from close enemies) in the most reasonable way possible.
     */
    public static boolean run(Unit unit) {

        // Define the range-wise closest enemy and run from it
        Unit nearestEnemy = SelectUnits.enemyRealUnits().nearestTo(unit);
        if (nearestEnemy != null) {
            if (nearestEnemy.distanceTo(nearestEnemy) <= 6.5) {
                unit.runFrom(nearestEnemy);
                return true;
            }
        } 
        
        // =========================================================
        // Try running to the main base
        else {
            Unit mainBase = SelectUnits.mainBase();
            if (mainBase != null && mainBase.distanceTo(unit) > 10) {
                unit.setTooltip("Run to base");
                unit.move(mainBase);
                return true;
            }
        }
        
        // Weird case: we didn't find a way to run.
        System.err.println("Weird case: we didn't find a way to run");
        return false;
    }

    /**
     * Indicates that this unit is not running any more.
     */
    public static void unitWantsStopRunning(Unit unit) {
        if (unit.getRunning().getTimeSinceLastRun() >= 13) {
            unit.getRunning().stopRunning();
        }
    }
    
}
