package atlantis.enemy;

import atlantis.information.AFoggedUnit;
import atlantis.map.AChoke;
import atlantis.map.AMap;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.strategy.EnemyUnitDiscoveredResponse;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnemyUnits {

    protected static Map<AUnit, AFoggedUnit> enemyUnitsDiscovered = new HashMap<>();
    private static Cache<Object> cache = new Cache<>();
    private static Cache<Boolean> cacheBoolean = new Cache<>();

    // =========================================================

    public static void updateFoggedUnits() {
        for (AUnit enemy : Select.enemy().list()) {
            EnemyUnits.updateEnemyUnitPosition(enemy);
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

    public static Selection selectFoggedUnits() {
        return Select.from(discoveredAndAliveUnits());
    }

    /**
     *
     */
//    public static APosition getLastPositionOfEnemyUnit(AUnit enemyUnit) {
//        return enemyUnitsDiscovered.containsKey(enemyUnit) ? enemyUnitsDiscovered.get(enemyUnit).position() : null;
//    }

    /**
     * Returns <b>true</b> if we have discovered at least one enemy building <b>(and it's still alive)</b>.
     */
    public static boolean hasDiscoveredAnyBuilding() {
        return cacheBoolean.get(
                "hasDiscoveredAnyBuilding",
                50,
                () -> {
                    for (AUnit enemyUnit : enemyUnitsDiscovered.values()) {
                        if (enemyUnit.isBuilding() && !UnitsArchive.isDestroyed(enemyUnit)) {
                            return true;
                        }
                    }
                    return false;
                }
        );
    }

    /**
     * Returns <b>true</b> if we have discovered at least one enemy building <b>(and it's still alive)</b>.
     */
    public static boolean hasDiscoveredAnyCombatUnit() {
        return cacheBoolean.get(
                "hasDiscoveredAnyCombatUnit",
                30,
                () -> {
                    for (AUnit enemyUnit : enemyUnitsDiscovered.values()) {
                        if (enemyUnit.isCombatUnit() && !UnitsArchive.isDestroyed(enemyUnit)) {
                            return true;
                        }
                    }
                    return false;
                }
        );
    }

    public static boolean discoveredEnemyBase() {
        return cacheBoolean.get(
                "discoveredEnemyBase",
                60,
                () -> enemyBase() != null
        );
    }

    public static APosition enemyBase() {
        return (APosition) cache.get(
                "enemyBase",
                30,
                () -> {
                    for (AFoggedUnit enemyUnit : enemyUnitsDiscovered.values()) {
                        if (enemyUnit.isBase()) {
                            return enemyUnit.position();
                        }
                    }
                    return null;
                }
        );
    }

    public static AFoggedUnit nearestEnemyBuilding() {
        return (AFoggedUnit) cache.get(
                "nearestEnemyBuilding",
                50,
                () -> {
                    AUnit ourMainBase = Select.main();
                    AFoggedUnit best = null;
                    if (ourMainBase != null) {
                        double minDist = 999999;

                        for (AFoggedUnit enemy : enemyUnitsDiscovered.values()) {
                            if (enemy.type().isBuilding() && enemy.position() != null) {
                                double dist = ourMainBase.groundDistance(enemy.position());
                                if (dist < minDist) {
                                    minDist = dist;
                                    best = enemy;
                                }
                            }
                        }
                    }

                    return best; // Can be null
                }
        );
    }

    public static Collection<AFoggedUnit> discoveredAndAliveUnits() {
        return enemyUnitsDiscovered.values();
    }

    // =========================================================
    // Number of units changed

    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void weDiscoveredEnemyUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.put(enemyUnit, new AFoggedUnit(enemyUnit));

        EnemyUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void removeDiscoveredUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.remove(enemyUnit);
    }

    /**
     * Forgets and refreshes info about given unit
     */
    public static void refreshEnemyUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.remove(enemyUnit);
        weDiscoveredEnemyUnit(enemyUnit);
    }

    /**
     * Updates last known position of the enemy unit.
     */
    public static void updateEnemyUnitPosition(AUnit enemyUnit) {
        if (!enemyUnit.type().isGasBuildingOrGeyser()) {
            return;
        }

        if (enemyUnitsDiscovered.containsKey(enemyUnit)) {
            enemyUnitsDiscovered.get(enemyUnit).update(enemyUnit);
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

    public static APosition enemyLocationOrGuess() {
        return (APosition) cache.get(
                "enemyLocationOrGuess",
                50,
                () -> {
                    APosition enemyBase = enemyBase();
                    if (enemyBase != null) {
                        return enemyBase.position();
                    }

                    AFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
                    if (enemyBuilding != null) {
                        return enemyBuilding.position();
                    }

                    AChoke enemyChoke = Chokes.enemyMainChoke();
                    if (enemyChoke != null) {
                        return enemyChoke.position();
                    }

                    APosition position = Bases.nearestUnexploredStartingLocation(Select.our().first());
                    if (position != null) {
                        return position;
                    }

                    return AMap.randomInvisiblePosition(Select.our().first().position());
                }
        );
    }

    public static boolean hasDefensiveLandBuilding() {
        return cacheBoolean.get(
                "hasDefensiveLandBuilding",
                30,
                () -> selectFoggedUnits()
                        .combatBuildings()
                        .excludeTypes(AUnitType.Zerg_Spore_Colony, AUnitType.Zerg_Creep_Colony)
                        .atLeast(1)
        );
    }
}
