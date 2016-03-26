package atlantis.combat.micro;

import java.util.List;

import atlantis.wrappers.Select;
import bwapi.Position;
import bwapi.Unit;

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
        return AtlantisRunning.runFrom(unit, null); // Run from the nearest enemy

//        // Define the range-wise closest enemy and run from it
//        Unit nearestEnemy = Select.enemyRealUnits().nearestTo(unit);
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
//            Unit mainBase = Select.mainBase();
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
        AtlantisRunning.stopRunning(unit); //unit.getRunning().stopRunning();
//        }
    }
    
    public static int getHowManyFramesUnitShouldStillBeRunning(Unit unit) {
        if (!AtlantisRunning.isRunning(unit)) {
            return 0;
        }
        else {
            return Math.max(0, defineMinFramesToStopRunning(unit) - AtlantisRunning.getTimeSinceLastRun(unit));
        }
    }
    
    // =========================================================

    private static int defineMinFramesToStopRunning(Unit unit) {
//        return MIN_TIME_FRAMES_TO_STOP_RUNNING + countNearbyUnits(unit) * 20 +
//                (100 - unit.getHPPercent()) / 2;
        return MIN_TIME_FRAMES_TO_STOP_RUNNING + countNearbyUnits(unit.getPosition()) * 10;
    }

    private static int countNearbyUnits(Position position) {
        int total = 0;
        List<Unit> unitsInRange = (List<Unit>) Select.our().inRadius(6, position).listUnits();	//TODO check cast safety
        for (Unit unit : unitsInRange) {
            if (!AtlantisRunning.isRunning(unit)) {
                total++;
            }
        }
        return total;
    }
    
}
