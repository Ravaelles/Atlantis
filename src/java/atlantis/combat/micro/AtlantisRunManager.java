package atlantis.combat.micro;

import atlantis.wrappers.SelectUnits;
import jnibwapi.Position;
import jnibwapi.Unit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisRunManager {

    private static int MIN_TIME_FRAMES_TO_STOP_RUNNING = 20;
    
    // =========================================================
    
    /**
     * Makes unit run (from close enemies) in the most reasonable way possible.
     */
    public static boolean run(Unit unit) {
        return unit.runFrom(null); // Run from the nearest enemy

//        // Define the range-wise closest enemy and run from it
//        Unit nearestEnemy = SelectUnits.enemyRealUnits().nearestTo(unit);
//        if (nearestEnemy != null) {
//            if (nearestEnemy.distanceTo(nearestEnemy) <= 6.5) {
//                unit.runFrom(nearestEnemy);
//                return true;
//            }
//        } 
//        
//        // =========================================================
//        // Try running to the main base
//        else {
//            Unit mainBase = SelectUnits.mainBase();
//            if (mainBase != null && mainBase.distanceTo(unit) > 10) {
//                unit.setTooltip("Run to base");
//                unit.move(mainBase);
//                return true;
//            }
//        }
//        
//        // Weird case: we didn't find a way to run.
//        System.err.println("Weird case: we didn't find a way to run");
//        return false;
    }

    /**
     * Indicates that this unit is not running any more.
     */
    public static void unitWantsStopRunning(Unit unit) {
//        if (unit.getRunning().getTimeSinceLastRun() >= defineMinFramesToStopRunning(unit)) {
        unit.getRunning().stopRunning();
//        }
    }
    
    public static int getHowManyFramesUnitShouldStillBeRunning(Unit unit) {
        if (!unit.isRunning()) {
            return 0;
        }
        else {
            return Math.max(0, defineMinFramesToStopRunning(unit) - unit.getRunning().getTimeSinceLastRun());
        }
    }
    
    // =========================================================

    private static int defineMinFramesToStopRunning(Unit unit) {
//        return MIN_TIME_FRAMES_TO_STOP_RUNNING + countNearbyUnits(unit) * 20 +
//                (100 - unit.getHPPercent()) / 2;
        return MIN_TIME_FRAMES_TO_STOP_RUNNING + countNearbyUnits(unit) * 10;
    }

    private static int countNearbyUnits(Position position) {
        int total = 0;
        for (Unit unit : SelectUnits.our().inRadius(6, position).list()) {
            if (!unit.isRunning()) {
                total++;
            }
        }
        return total;
    }
    
}
