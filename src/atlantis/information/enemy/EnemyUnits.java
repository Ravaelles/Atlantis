package atlantis.information.enemy;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
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
            .add(rawUnitsDiscovered())
//            .print("now with enemy")
            .removeDuplicates()
//            .print("after removing duplicates")
            .havingPosition();
//            .beingVisibleUnitOrNotVisibleFoggedUnit();
//            .print("and having position");
    }

    // =========================================================

    public static void clearCache() {
        cache.clear();
        enemyUnitsDiscovered.clear();
    }

    public static Collection<AbstractFoggedUnit> rawUnitsDiscovered() {
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
            () -> Select.from(rawUnitsDiscovered(), "foggedUnits")
        );
    }

    public static AUnit enemyBase() {
        return (AUnit) cache.get(
            "enemyBase",
            70,
            () -> discovered().bases().first()
        );
    }

    public static AUnit nearestEnemyBuilding() {
        return (AUnit) cache.get(
            "nearestEnemyBuilding",
            50,
            () -> {
                AUnit ourUnit = Select.main();
                if (ourUnit == null) {
                    ourUnit = Select.ourBuildings().first();
                }
                return discovered().buildings().nearestTo(ourUnit);
            }
        );
    }

    public static AUnit enemyWhoBreachedBase() {
        return EnemyWhoBreachedBase.get();
    }
}
