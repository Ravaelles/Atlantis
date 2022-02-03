package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.information.strategy.EnemyUnitDiscoveredResponse;
import atlantis.map.AChoke;
import atlantis.map.AMap;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AbstractFoggedUnit;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Cache;
import atlantis.util.Enemy;
import atlantis.util.We;

import java.util.Collection;

public class EnemyInfo {

    private static Cache<Object> cache = new Cache<>();
    private static Cache<Boolean> cacheBoolean = new Cache<>();

    public static boolean startedWithCombatBuilding = false;

    // =========================================================

    public static void clearCache() {
        cache.clear();
        cacheBoolean.clear();
        startedWithCombatBuilding = false;
    }

    public static boolean isEnemyNearAnyOurBuilding() {
        return enemyNearAnyOurBuilding() != null;
    }

    public static AUnit enemyNearAnyOurBuilding() {
        return (AUnit) cache.get(
                "enemyNearAnyOurBuilding",
                50,
                () -> {
                    if (!Have.base()) {
                        return null;
                    }

                    AUnit nearestEnemy = Select.enemyCombatUnits().excludeTypes(
                            AUnitType.Terran_Valkyrie,
                            AUnitType.Protoss_Observer,
                            AUnitType.Protoss_Corsair,
                            AUnitType.Zerg_Overlord,
                            AUnitType.Zerg_Scourge
                    ).nearestTo(Select.main());
                    if (nearestEnemy != null) {
                        return Select.ourBuildings()
                                .excludeTypes(AUnitType.Terran_Missile_Turret)
                                .inRadius(Enemy.terran() ? 13 : 7, nearestEnemy)
                                .atLeast(1)
                                ? nearestEnemy : null;
                    }

                    return null;
                }
        );
    }

    /**
     * Returns <b>true</b> if we have discovered at least one enemy building <b>(and it's still alive)</b>.
     */
    public static boolean hasDiscoveredAnyBuilding() {
        return cacheBoolean.get(
                "hasDiscoveredAnyBuilding",
                50,
                () -> {
                    for (AUnit enemyUnit : EnemyUnits.unitsDiscovered()) {
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
                    for (AUnit enemyUnit : EnemyUnits.unitsDiscovered()) {
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
                () -> EnemyUnits.enemyBase() != null
        );
    }

    public static Collection<AbstractFoggedUnit> discoveredAndAliveUnits() {
        return EnemyUnits.unitsDiscovered();
    }

    /**
     * Saves information about enemy unit that we see for the first time.
     */
    public static void weDiscoveredEnemyUnit(AUnit enemyUnit) {
        EnemyUnits.addFoggedUnit(enemyUnit);
        EnemyUnitDiscoveredResponse.updateEnemyUnitDiscovered(enemyUnit);
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void removeDiscoveredUnit(AUnit enemyUnit) {
        EnemyUnits.removeFoggedUnit(enemyUnit);
    }

    /**
     * Forgets and refreshes info about given unit
     */
    public static void refreshEnemyUnit(AUnit enemyUnit) {
        EnemyUnits.removeFoggedUnit(enemyUnit);
        weDiscoveredEnemyUnit(enemyUnit);
    }

    /**
     * Updates last known position of the enemy unit.
     */
    public static void updateEnemyUnitTypeAndPosition(AUnit enemyUnit) {
        if (enemyUnit.type().isGasBuildingOrGeyser()) {
            return;
        }

        AbstractFoggedUnit foggedUnit = EnemyUnits.getFoggedUnit(enemyUnit);
        if (foggedUnit != null) {
            foggedUnit.update(enemyUnit);
        }
    }

    // =========================================================

    /**
     * Returns number of discovered and alive enemy units of given type. Some of them (maybe even all of them)
     * may not be visible right now.
     */
//    public static int countEnemyKnownUnitsOfType(AUnitType type) {
//        int total = 0;
//        for (AUnit enemyUnit : EnemyUnits.unitsDiscovered()) {
//            if (enemyUnit.is(type)) {
//                total++;
//            }
//        }
//        return total;
//    }

    public static void printEnemyFoggedUnits() {
        Collection<AbstractFoggedUnit> foggedUnits = EnemyUnits.unitsDiscovered();
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
                    APosition enemyBase = EnemyUnits.enemyBase();
                    if (enemyBase != null) {
                        return enemyBase.position();
                    }

                    AbstractFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
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

                    return AMap.randomInvisiblePosition(Select.our().first());
                }
        );
    }

    public static boolean hasDefensiveLandBuilding(boolean onlyCompleted) {
        return cacheBoolean.get(
                "hasDefensiveLandBuilding:" + onlyCompleted,
                30,
                () -> {
                    Selection selection = EnemyUnits.foggedUnits()
                            .combatBuildings(false)
                            .excludeTypes(AUnitType.Zerg_Spore_Colony, AUnitType.Zerg_Creep_Colony);

                    if (onlyCompleted) {
                        selection = selection.onlyCompleted();
                    }

                    return selection.atLeast(1);
                }
        );
    }

    public static boolean isProxyBuilding(AbstractFoggedUnit enemyBuilding) {
        if (A.seconds() >= 400 || !We.haveBase()) {
            return false;
        }

        return Select.main().distToLessThan(enemyBuilding, 20);
    }

}
