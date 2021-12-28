package atlantis.enemy;

import atlantis.information.AFoggedUnit;
import atlantis.information.FakeFoggedUnit;
import atlantis.information.FoggedUnit;
import atlantis.position.APosition;
import atlantis.tests.unit.FakeUnit;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnemyUnits {

    private static Map<Integer, AFoggedUnit> enemyUnitsDiscovered = new HashMap<>();
    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static void updateFoggedUnits() {
        for (AUnit enemy : Select.enemy().list()) {
            EnemyInformation.updateEnemyUnitTypeAndPosition(enemy);
        }

//        for (FoggedUnit fogged : enemyUnitsDiscovered()) {
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

    public static void clearCache() {
        cache.clear();
        enemyUnitsDiscovered.clear();
    }

    public static void addFoggedUnit(AUnit enemyUnit) {
        AFoggedUnit foggedUnit = enemyUnit instanceof FakeUnit
                ? FakeFoggedUnit.fromFake((FakeUnit) enemyUnit)
                : FoggedUnit.from(enemyUnit);
        enemyUnitsDiscovered.put(enemyUnit.id(), foggedUnit);
    }

    public static void remove(AUnit enemyUnit) {
        enemyUnitsDiscovered.remove(enemyUnit.id());
    }

    public static boolean isKnown(AUnit enemyUnit) {
        return enemyUnitsDiscovered.containsKey(enemyUnit.id());
    }

    public static AFoggedUnit getFoggedUnit(AUnit enemyUnit) {
        return enemyUnitsDiscovered.get(enemyUnit.id());
    }

    public static Collection<AFoggedUnit> unitsDiscovered() {
        return enemyUnitsDiscovered.values();
    }

    public static Selection unitsDiscoveredSelection() {
        return Select.from(unitsDiscovered(), "");
    }

    // =========================================================

    public static Selection foggedUnits() {
        return Select.from(EnemyInformation.discoveredAndAliveUnits(), "foggedUnits");
    }

    public static APosition enemyBase() {
        return (APosition) cache.get(
                "enemyBase",
                30,
                () -> {
                    for (AFoggedUnit enemyUnit : unitsDiscovered()) {
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

                        for (AFoggedUnit enemy : unitsDiscovered()) {
                            if (enemy.type().isBuilding() && enemy.position() != null) {
                                double dist = ourMainBase.groundDist(enemy.position());
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

//    public static Selection combatBuildings(boolean includeCreepColonies) {
//        return (Selection) cache.get(
//                "combatBuildings:" + A.trueFalse(includeCreepColonies),
//                40,
//                () -> selectFoggedUnits().combatBuildings(includeCreepColonies)
//        );
//    }

    public static Selection combatUnitsToBetterAvoid() {
        return (Selection) cache.get(
                "combatUnitsToBetterAvoid:",
                40,
                () -> {
                    Selection combatUnits = foggedUnits().combatUnits();

                    return combatUnits.clone().combatBuildings(false).add(
                            combatUnits.clone().ofType(
                                AUnitType.Terran_Siege_Tank_Siege_Mode,
                                AUnitType.Zerg_Lurker
                            )
                    );
                }
        );
    }

}
