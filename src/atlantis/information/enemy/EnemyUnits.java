package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.*;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;
import tests.unit.FakeUnit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnemyUnits {

    private static Map<Integer, AbstractFoggedUnit> enemyUnitsDiscovered = new HashMap<>();
    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static void updateFoggedUnits() {
        for (AUnit enemy : Select.enemy().list()) {
            EnemyInfo.updateEnemyUnitTypeAndPosition(enemy);
        }
    }

    public static void clearCache() {
        cache.clear();
        enemyUnitsDiscovered.clear();
    }

    public static void addFoggedUnit(AUnit enemyUnit) {
        AbstractFoggedUnit foggedUnit = enemyUnit instanceof FakeUnit
                ? FakeFoggedUnit.fromFake((FakeUnit) enemyUnit)
                : FoggedUnit.from(enemyUnit);

        enemyUnitsDiscovered.put(enemyUnit.id(), foggedUnit);
    }

    public static void removeFoggedUnit(AUnit enemyUnit) {
//        if (enemyUnit.isAlive()) {
//            System.err.println("Removing fogged unit = " + enemyUnit);
//        }
//        A.printStackTrace("Removing fogged unit = " + enemyUnit);

        enemyUnitsDiscovered.remove(enemyUnit.id());
        cache.clear();
    }

//    public static boolean isKnown(AUnit enemyUnit) {
//        return enemyUnitsDiscovered.containsKey(enemyUnit.id());
//    }

    public static AbstractFoggedUnit getFoggedUnit(AUnit enemyUnit) {
        return enemyUnitsDiscovered.get(enemyUnit.id());
    }

    public static Collection<AbstractFoggedUnit> unitsDiscovered() {
        return enemyUnitsDiscovered.values();
    }

    public static Selection unitsDiscoveredSelection() {
        return Select.from(unitsDiscovered(), "");
    }

    // =========================================================

    public static Selection foggedUnits() {
        return Select.from(EnemyInfo.discoveredAndAliveUnits(), "foggedUnits");
    }

    public static APosition enemyBase() {
        return (APosition) cache.get(
                "enemyBase",
                70,
                () -> {
                    for (AbstractFoggedUnit enemyUnit : unitsDiscovered()) {
                        if (enemyUnit.isBase()) {
                            return enemyUnit.position();
                        }
                    }
                    return null;
                }
        );
    }

    public static AbstractFoggedUnit nearestEnemyBuilding() {
        return (AbstractFoggedUnit) cache.get(
                "nearestEnemyBuilding",
                50,
                () -> {
                    AUnit ourMainBase = Select.main();
                    AbstractFoggedUnit best = null;
                    if (ourMainBase != null) {
                        double minDist = 999999;

                        for (AbstractFoggedUnit enemy : unitsDiscovered()) {
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
                30,
                () -> {
                    Selection foggedCombatnits = foggedUnits()
                            .combatUnits()
                            .havingPosition();

                    return foggedCombatnits
                            .clone()
                            .combatBuildings(false)
                            .add(
                                foggedCombatnits.clone().ofType(
                                    AUnitType.Protoss_Photon_Cannon,
                                    AUnitType.Terran_Siege_Tank_Siege_Mode,
                                    AUnitType.Zerg_Lurker,
                                    AUnitType.Zerg_Sunken_Colony
                                )
                            )
                            .havingPosition();
                }
        );
    }

}
