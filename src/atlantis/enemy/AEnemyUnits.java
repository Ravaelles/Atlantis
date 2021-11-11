package atlantis.enemy;

import atlantis.information.AFoggedUnit;
import atlantis.position.APosition;
import atlantis.strategy.EnemyUnitDiscoveredResponse;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

import java.util.*;
import java.util.stream.Collectors;

public class AEnemyUnits {

    protected static Map<AUnit, AFoggedUnit> enemyUnitsDiscovered = new HashMap<>();

    // =========================================================

    public static void updateFoggedUnits() {
        for (AUnit enemy : Select.enemy().list()) {
            AEnemyUnits.updateEnemyUnitPosition(enemy);
        }

//        for (AFoggedUnit fogged : enemyUnitsDiscovered.values()) {
////            System.err.println(fogged + " // " + fogged.isBuilding() + " // " + fogged.getPosition().isVisible() + " // " +fogged.isVisibleOnMap());
//            if (
//                    !fogged.isBuilding()
//                            && fogged.hasKnownPosition()
//                            && fogged.position().isVisible()
//                            && !fogged.isVisibleOnMap()
//            ) {
//                fogged.positionUnknown();
////                System.out.println("      " + fogged + " position now  unknown");
//            }
//        }
    }

    // =========================================================

    /**
     *
     */
//    public static APosition getLastPositionOfEnemyUnit(AUnit enemyUnit) {
//        return enemyUnitsDiscovered.containsKey(enemyUnit) ? enemyUnitsDiscovered.get(enemyUnit).position() : null;
//    }
    
    /**
     * Returns <b>true</b> if we have discovered at least one enemy building <b>(and it's still alive)</b>.
     */
    public static boolean hasDiscoveredAnyEnemyBuilding() {
        for (AUnit enemyUnit : enemyUnitsDiscovered.values()) {
            if (enemyUnit.isBuilding()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if we've discovered the main base of enemy (natural base doesn't count).
     */
    public static boolean hasDiscoveredEnemyBuilding() {
        
        // We don't know any enemy building
        if (!AEnemyUnits.hasDiscoveredAnyEnemyBuilding()) {
            return false;
        }
        
//        System.out.println("-------");
        for (AFoggedUnit enemyUnitData : AEnemyUnits.discoveredAndAliveUnits()) {
//            System.out.println(enemyUnitData.getType());
            if (enemyUnitData.type().isBuilding()) {
                return true;
////                boolean isBaseAtStartingLocation = false;
//                APosition building = enemyUnitData.position();
//
//                for (ABaseLocation startingLocation : Bases.startingLocations(false)) {
//                    if (building.distTo(startingLocation.position()) <= 7) {
////                        System.out.println("Discovered main enemy base");
//                        return true;
//                    }
////                    else {
////                        System.out.println("Ha! This ain't main enemy base!");
////                    }
//                }
            }
        }
        
        return false;
    }
    
    public static APosition enemyBase() {
        for (AFoggedUnit enemyUnit : enemyUnitsDiscovered.values()) {
            if (enemyUnit.isBase()) {
//                return getLastPositionOfEnemyUnit(enemyUnit);
                return enemyUnit.position();
            }
        }
        return null;
    }
    
    public static AFoggedUnit nearestEnemyBuilding() {
        AUnit ourMainBase = Select.main();
        AFoggedUnit best = null;
        if (ourMainBase != null) {
            double minDist = 999999;
            
            for (AFoggedUnit enemy : enemyUnitsDiscovered.values()) {
                if (enemy.type().isBuilding() && enemy.position() != null) {
                    double dist = ourMainBase.distTo(enemy.position());
                    if (dist < minDist) {
                        minDist = dist;
                        best = null;
                    }
                }
            }
        }
        
        return best; // Can be null
    }
    
    public static Collection<AFoggedUnit> discoveredAndAliveUnits() {
        return enemyUnitsDiscovered.values();
    }
    
    // =========================================================
    // Number of units changed

    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void discoveredEnemyUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.put(enemyUnit, new AFoggedUnit(enemyUnit));

        EnemyUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void removeDiscoveredUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.remove(enemyUnit);
//        enemyUnitsDestroyed.put(enemyUnit.id(), enemyUnit);
    }
    
    /**
     * Returns <b>true</b> if enemy unit has been destroyed and we know it.
     */
    public static boolean isEnemyUnitDestroyed(AUnit enemyUnit) {
        return UnitsArchive.isDestroyed(enemyUnit.id());
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
        if (!enemyUnit.type().isGasBuildingOrGeyser()) {
            return;
        }

//        if (enemyUnit.isLurker()) {
//            System.out.println(enemyUnit);
//            System.out.println(enemyUnit.x() + " // " + enemyUnit._lastX);
//            System.out.println(enemyUnit.y() + " // " + enemyUnit._lastY);
//        }

//        enemyUnitsDiscovered.get(enemyUnit).updatePosition(enemyUnit.getPosition());
        if (enemyUnitsDiscovered.containsKey(enemyUnit)) {
            enemyUnitsDiscovered.get(enemyUnit).update(enemyUnit);
        }
//        else {
//            System.err.println("No fogged unit previously: " + enemyUnit);
//        }
    }

    public static List<AFoggedUnit> foggedUnits() {
//        ArrayList<AFoggedUnit> foggedUnits = new ArrayList<>();
//
//        for (AFoggedUnit unit : enemyUnitsDiscovered.values()) {
//
//        }

        return (new ArrayList<>(enemyUnitsDiscovered.values()))
                .stream()
                .filter(u -> u.isAccessible())
                .collect(Collectors.toList());
    }

    // =========================================================
    // COUNT
    
    /**
     * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them)
     * may not be visible right now.
     */
    public static int countEnemyKnownUnitsOfType(AUnitType type) {
        int total = 0;
        for (AUnit enemyUnit : enemyUnitsDiscovered.values()) {
            if (enemyUnit.isType(type)) {
                total++;
            }
        }
        return total;
    }

    public static void printEnemyFoggedUnits() {
        Collection<AFoggedUnit> foggedUnits = enemyUnitsDiscovered.values();
        if (!foggedUnits.isEmpty()) {
            System.out.println("--- Enemy fogged units (" + foggedUnits.size() + ") ---");
            for (AUnit fogged : foggedUnits) {
                System.out.println(
                        fogged.type()
                                + " " + fogged.position()
                                + ", isBase=" + fogged.isBase()
                                + ", alive=" + fogged.isAlive()
                );
            }
        }
    }

}
