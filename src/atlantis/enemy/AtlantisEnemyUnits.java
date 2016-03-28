package atlantis.enemy;

import atlantis.AtlantisConfig;
import atlantis.AtlantisGame;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.wrappers.MappingCounter;
import atlantis.units.Select;

import bwapi.UnitType;
import java.util.ArrayList;

public class AtlantisEnemyUnits {

    protected static ArrayList<AUnit> enemyUnitsDiscovered = new ArrayList<>();
    protected static ArrayList<AUnit> enemyUnitsDestroyed = new ArrayList<>();

    // =========================================================
    // Special methods
    
    /**
     * Forgets and refreshes info about given unit
     */
    public static void refreshEnemyUnit(AUnit enemyUnit) {
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
    public static void forgetUnit(AUnit enemyUnit) {
        if (enemyUnit != null) {
            enemyUnitsDiscovered.remove(enemyUnit);
        }
    }

    /**
     * Based on a stored collection, returns unit object for given unitID.
     */
    public static AUnit getEnemyUnitByID(int unitID) {
        for (AUnit unit : enemyUnitsDiscovered) {
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
    public static void discoveredEnemyUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.add(enemyUnit);
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void unitDestroyed(AUnit enemyUnit) {
//        System.out.println("Destroyed " + unit + " / " + unit.getID() + " / enemy: " + unit.isEnemy());
        enemyUnitsDestroyed.add(enemyUnit);
    }
    
    /**
     * Returns <b>true</b> if enemy unit has been destroyed and we know it.
     */
    public static boolean isEnemyUnitDestroyed(AUnit enemyUnit) {
        return enemyUnitsDestroyed.contains(enemyUnit);
    }

    // =========================================================
    // ESTIMATE
    
    /**
     * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them)
     * may not be visible right now.
     */
    public static int estimateEnemyUnitsOfType(AUnitType type) {
        int total = 0;
        for (AUnit unit : enemyUnitsDiscovered) {
            if (unit.getType().equals(type)) {
                total++;
            }
        }
        return total;
    }

}
