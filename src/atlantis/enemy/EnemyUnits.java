package atlantis.enemy;

import atlantis.information.AFoggedUnit;
import atlantis.position.APosition;
import atlantis.units.AUnit;
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

    // =========================================================

    public static Selection selectFoggedUnits() {
        return Select.from(EnemyInformation.discoveredAndAliveUnits());
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

    public static Selection combatBuildings() {
        return (Selection) cache.get(
                "combatBuildings",
                30,
                () -> selectFoggedUnits().combatBuildings()
        );
    }
}
