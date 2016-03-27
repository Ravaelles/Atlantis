package atlantis.enemy;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.wrappers.MappingCounter;
import atlantis.wrappers.Select;
import bwapi.Unit;
import bwapi.UnitType;
import java.util.ArrayList;

public class AtlantisEnemyUnits {

    protected static ArrayList<Unit> enemyUnitsDiscovered = new ArrayList<>();
    protected static ArrayList<Unit> enemyUnitsDestroyed = new ArrayList<>();

    // =========================================================
    // Special methods
    
    /**
     * Forgets and refreshes info about given unit
     */
    public static void refreshEnemyUnit(Unit enemyUnit) {
        forgetUnit(enemyUnit);
        
//        if (enemyUnit.getPlayer().isEnemy(enemyUnit.getPlayer())) {
        if (enemyUnit.isEnemy()) {
            discoveredEnemyUnit(enemyUnit);
        }
    }

    /**
     * Informs this class that new (possibly unfinished) unit exists in the game. Both our (including
     * unfinished) and enemy's.
     */
    public static void forgetUnit(Unit enemyUnit) {
        if (enemyUnit != null) {
            enemyUnitsDiscovered.remove(enemyUnit);
        }
    }

    /**
     * Based on a stored collection, returns unit object for given unitID.
     */
    public static Unit getEnemyUnitByID(int unitID) {
        for (Unit unit : enemyUnitsDiscovered) {
            if (unit.getID() == unitID) {
                return unit;
            }
        }

        return null;
    }

    // =========================================================
    // Number of units changed
    
    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void discoveredEnemyUnit(Unit enemyUnit) {
        enemyUnitsDiscovered.add(enemyUnit);
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void unitDestroyed(Unit enemyUnit) {
//        System.out.println("Destroyed " + unit + " / " + unit.getID() + " / enemy: " + unit.isEnemy());
        enemyUnitsDestroyed.add(enemyUnit);
    }
    
    /**
     * Returns <b>true</b> if enemy unit has been destroyed and we know it.
     */
    public static boolean isEnemyUnitDestroyed(Unit enemyUnit) {
        return enemyUnitsDestroyed.contains(enemyUnit);
    }

    // =========================================================
    // ESTIMATE
    
    /**
     * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them)
     * may not be visible right now.
     */
    public static int estimateEnemyUnitsOfType(UnitType type) {
        int total = 0;
        for (Unit unit : enemyUnitsDiscovered) {
            if (unit.getType().equals(type)) {
                total++;
            }
        }
        return total;
    }

}
