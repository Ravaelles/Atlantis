package atlantis.information.enemy;

import atlantis.map.position.APosition;
import atlantis.units.*;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnemyUnits {

    protected static Map<Integer, AbstractFoggedUnit> enemyUnitsDiscovered = new HashMap<>();
    protected static Cache<Object> cache = new Cache<>();

    // =========================================================

    /**
     * Both visible units and those behind fog of war.
     */
    public static Selection discovered() {
//        return Select.from(Select.enemy(), "")
        return Select.enemy()
//            .print("visibleAndFogged")
            .add(unitsDiscovered())
//            .print("now with enemy")
            .removeDuplicates()
//            .print("now after removal")
            .havingPosition();
//            .print("and having position?");
    }

    // =========================================================

    public static void clearCache() {
        cache.clear();
        enemyUnitsDiscovered.clear();
    }

    public static Collection<AbstractFoggedUnit> unitsDiscovered() {
        return enemyUnitsDiscovered.values();
    }

    public static AbstractFoggedUnit getFoggedUnit(AUnit enemyUnit) {
        return enemyUnitsDiscovered.get(enemyUnit.id());
    }

    // =========================================================

    public static boolean has(AUnitType unit) {
        return EnemyUnits.discovered().countOfType(unit) > 0;
    }

    public static int count(AUnitType type) {
        return discovered().ofType(type).count();
    }

    public static Selection foggedUnits() {
        return (Selection) cache.get(
            "foggedUnits",
            0,
            () -> Select.from(unitsDiscovered(), "foggedUnits")
        );
    }

    public static AUnit enemyBase() {
        return (AUnit) cache.get(
                "enemyBase",
                70,
                () -> {
                    for (AbstractFoggedUnit enemyUnit : unitsDiscovered()) {
                        if (enemyUnit.isBase()) {
                            return enemyUnit;
                        }
                    }
                    return null;
                }
        );
    }

    public static AUnit nearestEnemyBuilding() {
        return (AUnit) cache.get(
                "nearestEnemyBuilding",
                50,
                () -> {
                    AUnit ourMainBase = Select.main();
                    AUnit best = null;
                    if (ourMainBase != null) {
                        double minDist = 999999;

//                        for (AbstractFoggedUnit enemy : unitsDiscovered()) {
                        for (AUnit enemy : discovered().list()) {
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

//    public static Selection combatUnitsToBetterAvoid() {
//        return (Selection) cache.get(
//                "combatUnitsToBetterAvoid:",
//                30,
//                () -> {
//                    Selection foggedCombatnits = foggedUnits()
//                            .combatUnits()
//                            .havingPosition();
//
//                    return foggedCombatnits
//                            .clone()
//                            .combatBuildings(false)
//                            .add(
//                                foggedCombatnits.clone().ofType(
//                                    AUnitType.Protoss_Photon_Cannon,
//                                    AUnitType.Terran_Siege_Tank_Siege_Mode,
//                                    AUnitType.Zerg_Lurker,
//                                    AUnitType.Zerg_Sunken_Colony
//                                )
//                            )
//                            .havingPosition();
//                }
//        );
//    }

}
