package atlantis.information.enemy;

import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.strategy.EnemyUnitDiscoveredResponse;
import atlantis.map.position.APosition;
import atlantis.units.*;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;
import bwapi.Color;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnemyUnits {

    protected static Map<Integer, AbstractFoggedUnit> enemyUnitsDiscovered = new HashMap<>();
    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static void updateFoggedUnits() {
//        System.out.println("--- UPDATE at " + A.now());
        for (AUnit enemy : Select.enemy().list()) {
//            System.out.println("update fogged from real = " + enemy);
            updateUnitTypeAndPosition(enemy);
        }

        for (AbstractFoggedUnit foggedUnit : enemyUnitsDiscovered.values()) {
//            System.out.println("update fogged = " + foggedUnit);
            updatedFogged(foggedUnit);
        }
    }

    public static boolean updateUnitTypeAndPosition(AUnit enemy) {
        if (enemy.type().isGasBuildingOrGeyser()) {
            return true;
        }

        AbstractFoggedUnit foggedUnit = getFoggedUnit(enemy);
        if (foggedUnit != null) {
            foggedUnit.update(enemy);
        }
        return false;
    }

    /**
     * Check if the position of fogged unit is visible and there is no unit there.
     * If so, change it, because it means we don't know where it is.
     */
    private static void updatedFogged(AbstractFoggedUnit foggedUnit) {
        AUnit aUnit = foggedUnit.innerAUnit();
//        System.out.println(aUnit + " // visible: " + (aUnit != null ? aUnit.isVisibleUnitOnMap() : "---"));
        if (aUnit == null || !aUnit.isVisibleUnitOnMap()) {
//            if (foggedUnit.hasPosition()) {
//                APainter.paintCircleFilled(
//                    foggedUnit,
//                    8,
//                    foggedUnit.position().isPositionVisible() ? Color.Green : Color.Red
//                );
//            }

            if (foggedUnit.hasPosition() && foggedUnit.position().isPositionVisible()) {
//                System.out.println(">> Fogged unit is no longer visible, remove position " + foggedUnit);
                foggedUnit.removeKnownPosition();
            }
        }
    }

    // =========================================================

    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void weDiscoveredEnemyUnit(AUnit enemyUnit) {
        addFoggedUnit(enemyUnit);
        EnemyUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
    }

    // =========================================================

    public static void clearCache() {
        cache.clear();
        enemyUnitsDiscovered.clear();
    }

    public static Collection<AbstractFoggedUnit> unitsDiscovered() {
        return enemyUnitsDiscovered.values();
    }

    public static void addFoggedUnit(AUnit enemyUnit) {
        AbstractFoggedUnit foggedUnit = AbstractFoggedUnit.from(enemyUnit);

        enemyUnitsDiscovered.put(enemyUnit.id(), foggedUnit);
    }

    public static void removeFoggedUnit(AUnit enemyUnit) {
        enemyUnitsDiscovered.remove(enemyUnit.id());
        cache.clear();
    }

    public static AbstractFoggedUnit getFoggedUnit(AUnit enemyUnit) {
        return enemyUnitsDiscovered.get(enemyUnit.id());
    }

    public static Selection visibleAndFogged() {
        return Select.from(unitsDiscovered(), "")
//            .print("visibleAndFogged")
            .add(Select.enemy())
//            .print("now with enemy")
            .removeDuplicates()
//            .print("now after removal")
            .havingPosition();
//            .print("and having position?");
    }

    // =========================================================

    public static int count(AUnitType type) {
        return visibleAndFogged().ofType(type).count();
    }

    public static Selection foggedUnits() {
        return (Selection) cache.get(
            "foggedUnits",
            0,
            () -> Select.from(unitsDiscovered(), "foggedUnits")
        );
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
