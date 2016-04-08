package atlantis.enemy;

import atlantis.information.UnitData;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.wrappers.APosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AtlantisEnemyUnits {

    protected static Map<AUnit, UnitData> enemyUnitsDiscovered = new HashMap<>();
    protected static ArrayList<AUnit> enemyUnitsDestroyed = new ArrayList<>();

    // =========================================================
    // Top abstraction methods
    
    /**
     *
     */
    public static APosition getLastPositionOfEnemyUnit(AUnit enemyUnit) {
        return enemyUnitsDiscovered.get(enemyUnit).getPosition();
    }
    
    /**
     * Returns <b>true</b> if we have discovered at least one enemy building <b>(and it's still alive)</b>.
     */
    public static boolean hasDiscoveredEnemyBuilding() {
        for (AUnit enemyUnit : enemyUnitsDiscovered.keySet()) {
            if (enemyUnit.isBuilding()) {
                return true;
            }
        }
        return false;
    }
    
    public static APosition getEnemyBase() {
        for (AUnit enemyUnit : enemyUnitsDiscovered.keySet()) {
            if (enemyUnit.isBase()) {
                return getLastPositionOfEnemyUnit(enemyUnit);
            }
        }
        return null;
    }
    
    public static UnitData getNearestEnemyBuilding() {
        AUnit ourMainBase = Select.mainBase();
        UnitData best = null;
        if (ourMainBase != null) {
            double minDist = 999999;
            
            for (UnitData enemy : enemyUnitsDiscovered.values()) {
                if (enemy.getType().isBuilding()) {
                    double dist = enemy.getPosition().distanceTo(ourMainBase);
                    if (minDist > dist) {
                        minDist = dist;
                        best = null;
                    }
                }
            }
        }
        
        if (best != null) {
            return best;
        }
        else {
            return null;
        }
    }
    
    public static Collection<UnitData> getDiscoveredAndAliveUnits() {
        return enemyUnitsDiscovered.values();
    }
    
    // =========================================================
    // Number of units changed

    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void discoveredEnemyUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.put(enemyUnit, new UnitData(enemyUnit));
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void unitDestroyed(AUnit enemyUnit) {
        enemyUnitsDestroyed.add(enemyUnit);
    }
    
    /**
     * Returns <b>true</b> if enemy unit has been destroyed and we know it.
     */
    public static boolean isEnemyUnitDestroyed(AUnit enemyUnit) {
        return enemyUnitsDestroyed.contains(enemyUnit);
    }
    
    /**
     * Forgets and refreshes info about given unit
     */
    public static void refreshEnemyUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.remove(enemyUnit);
        if (enemyUnit.isEnemy()) {
            discoveredEnemyUnit(enemyUnit);
        }
    }

    // =========================================================
    // COUNT
    
    /**
     * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them)
     * may not be visible right now.
     */
    public static int countEnemyKnownUnitsOfType(AUnitType type) {
        int total = 0;
        for (AUnit enemyUnit : enemyUnitsDiscovered.keySet()) {
            if (enemyUnit.isType(type)) {
                total++;
            }
        }
        return total;
    }

}
