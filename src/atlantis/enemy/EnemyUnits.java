package atlantis.enemy;

import atlantis.information.AFoggedUnit;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;

import java.util.HashMap;
import java.util.Map;

public class EnemyUnits {

    protected static Map<AUnit, AFoggedUnit> enemyUnitsDiscovered = new HashMap<>();
    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static void updateFoggedUnits() {
        for (AUnit enemy : Select.enemy().list()) {
            EnemyInformation.updateEnemyUnitPosition(enemy);
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

    public static void clearCache() {
        cache.clear();
        enemyUnitsDiscovered.clear();
    }

    // =========================================================

    public static Selection foggedUnits() {
        return Select.from(EnemyInformation.discoveredAndAliveUnits(), "foggedUnits");
    }

    /**
     *
     */
//    public static APosition getLastPositionOfEnemyUnit(AUnit enemyUnit) {
//        return enemyUnitsDiscovered.containsKey(enemyUnit) ? enemyUnitsDiscovered.get(enemyUnit).position() : null;
//    }

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
