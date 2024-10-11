package atlantis.information.enemy;

import atlantis.map.base.BaseLocations;
import atlantis.map.base.define.EnemyMainBase;
import atlantis.map.position.HasPosition;
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
        return (Selection) cache.get(
            "discovered",
            0,
            () -> {
                //        return Select.from(Select.enemy(), "")
                return Select.enemy()
                    .notDeadMan()
//            .print("visibleAndFogged")
                    .add(rawUnitsDiscovered())
//            .print("now with enemy")
                    .removeDuplicates()
//            .print("after removing duplicates")
                    .havingPosition();
//            .beingVisibleUnitOrNotVisibleFoggedUnit();
//            .print("and having position");
            }
        );
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
        return (AUnit) cache.getIfValid(
            "enemyBase",
            71,
            () -> discovered().bases().first()
        );
    }

    public static HasPosition enemyMainBase() {
        return EnemyMainBase.get();
    }

    public static AUnit nearestEnemyBuilding() {
        return (AUnit) cache.getIfValid(
            "nearestEnemyBuilding",
            111,
            () -> {
                AUnit ourUnit = Select.mainOrAnyBuilding();
                return discovered().buildings().groundNearestTo(ourUnit);
            }
        );
    }

    public static AUnit nearestEnemyCombatBuilding() {
        return (AUnit) cache.getIfValid(
            "nearestEnemyCombatBuilding",
            51,
            () -> {
                AUnit ourUnit = Select.mainOrAnyBuilding();
                return discovered().combatBuildingsAntiLand().groundNearestTo(ourUnit);
            }
        );
    }

    public static AUnit enemyWhoBreachedBase() {
        return EnemyUnitBreachedBase.get();
    }

    public static int dragoons() {
        return discovered().dragoons().count();
    }

    public static int hydras() {
        return discovered().hydras().count();
    }

    public static int mutas() {
        return discovered().mutalisks().count();
    }

    public static int zealots() {
        return discovered().zealots().count();
    }

    public static int zerglings() {
        return discovered().zerglings().count();
    }

    public static int combatUnits() {
        return discovered().combatUnits().excludeOverlords().count();
    }

    public static Selection buildings() {
        return discovered().buildings();
    }
}
