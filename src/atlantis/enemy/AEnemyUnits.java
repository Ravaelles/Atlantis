package atlantis.enemy;

import atlantis.information.AFoggedUnit;
import atlantis.information.AMap;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwta.BaseLocation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AEnemyUnits {

    protected static Map<AUnit, AFoggedUnit> enemyUnitsDiscovered = new HashMap<>();
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
    public static boolean hasDiscoveredAnyEnemyBuilding() {
        for (AUnit enemyUnit : enemyUnitsDiscovered.keySet()) {
            if (enemyUnit.isBuilding()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if we've discovered the main base of enemy (natural base doesn't count).
     */
    public static boolean hasDiscoveredMainEnemyBase() {
        
        // We don't know any enemy building
        if (!AEnemyUnits.hasDiscoveredAnyEnemyBuilding()) {
            return false;
        }
        
//        System.out.println("-------");
        for (AFoggedUnit enemyUnitData : AEnemyUnits.getEnemyDiscoveredAndAliveUnits()) {
//            System.out.println(enemyUnitData.getType());
            if (enemyUnitData.getType().isBase()) {
                boolean isBaseAtStartingLocation = false;
                APosition discoveredBase = enemyUnitData.getPosition();
                
                for (BaseLocation startingLocation : AMap.getStartingLocations(false)) {
                    if (discoveredBase.distanceTo(startingLocation.getPosition()) <= 7) {
//                        System.out.println("Discovered main enemy base");
                        return true;
                    }
//                    else {
//                        System.out.println("Ha! This ain't main enemy base!");
//                    }
                }
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
    
    public static AFoggedUnit getNearestEnemyBuilding() {
        AUnit ourMainBase = Select.mainBase();
        AFoggedUnit best = null;
        if (ourMainBase != null) {
            double minDist = 999999;
            
            for (AFoggedUnit enemy : enemyUnitsDiscovered.values()) {
                if (enemy.getType().isBuilding()) {
                    double dist = enemy.getPosition().distanceTo(ourMainBase);
                    if (minDist > dist) {
                        minDist = dist;
                        best = null;
                    }
                }
            }
        }
        
        return best; // Can be null
    }
    
    public static Collection<AFoggedUnit> getEnemyDiscoveredAndAliveUnits() {
        return enemyUnitsDiscovered.values();
    }
    
    // =========================================================
    // Number of units changed

    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void discoveredEnemyUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.put(enemyUnit, new AFoggedUnit(enemyUnit));
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void unitDestroyed(AUnit enemyUnit) {
        enemyUnitsDiscovered.remove(enemyUnit);
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
        discoveredEnemyUnit(enemyUnit);
    }
    
    /**
     * Updates last known position of the enemy unit.
     */
    public static void updateEnemyUnitPosition(AUnit enemyUnit) {
        enemyUnitsDiscovered.get(enemyUnit).updatePosition(enemyUnit.getPosition());
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
