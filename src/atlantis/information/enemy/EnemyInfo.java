package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.map.AChoke;
import atlantis.map.AMap;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;
import atlantis.util.Enemy;
import atlantis.util.We;

public class EnemyInfo {

    public static boolean startedWithCombatBuilding = false;

    private static Cache<Object> cache = new Cache<>();
    private static Cache<Boolean> cacheBoolean = new Cache<>();

    // =========================================================

    public static void clearCache() {
        cache.clear();
        cacheBoolean.clear();
        startedWithCombatBuilding = false;
    }

    public static boolean isEnemyNearAnyOurBase() {
        return enemyNearAnyOurBase(-1) != null;
    }

    public static AUnit enemyNearAnyOurBase(int maxDistToBase) {
        if (maxDistToBase < 0) {
            maxDistToBase = 12;
        }
        int finalMaxDistToBase = maxDistToBase;

        return (AUnit) cache.get(
                "enemyNearAnyOurBuilding:" + maxDistToBase,
                45,
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
                        return Select.ourBases()
//                                .inRadius(Enemy.terran() ? 22 : 17, nearestEnemy).atLeast(1)
                                .inRadius(finalMaxDistToBase, nearestEnemy).atLeast(1)
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
                    for (AUnit enemyUnit : EnemyUnits.discovered().list()) {
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
                    for (AUnit enemyUnit : EnemyUnits.discovered().list()) {
                        if (enemyUnit.isCombatUnit() && !UnitsArchive.isDestroyed(enemyUnit)) {
                            return true;
                        }
                    }
                    return false;
                }
        );
    }

    public static boolean hasDiscoveredEnemyBase() {
        return cacheBoolean.get(
                "hasDiscoveredEnemyBase",
                60,
                () -> EnemyUnits.enemyBase() != null
        );
    }

    /**
     * Saves information about given unit being destroyed, so counting units works properly.
     */
    public static void removeDiscoveredUnit(AUnit enemyUnit) {
        EnemyUnitsUpdater.removeFoggedUnit(enemyUnit);
    }

    /**
     * Forgets and refreshes info about given unit
     */
    public static void refreshEnemyUnit(AUnit enemyUnit) {
        EnemyUnitsUpdater.removeFoggedUnit(enemyUnit);
        EnemyUnitsUpdater.weDiscoveredEnemyUnit(enemyUnit);
    }

    public static APosition enemyLocationOrGuess() {
        return (APosition) cache.get(
                "enemyLocationOrGuess",
                50,
                () -> {
                    AUnit enemyBase = EnemyUnits.enemyBase();
                    if (enemyBase != null) {
                        return enemyBase.position();
                    }

                    AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
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

    public static AUnit enemyUnitInMainBase() {
        AUnit main = Select.main();
        if (main == null) {
            return null;
        }

        return Select.enemyCombatUnits().inRadius(20, main).nearestTo(main);
    }

    public static boolean isProxyBuilding(AUnit enemyBuilding) {
        if (A.seconds() >= 400 || !We.haveBase()) {
            return false;
        }

        return Select.main().distToLessThan(enemyBuilding, 20);
    }

    public static boolean isDoingEarlyGamePush() {
        return cacheBoolean.get(
            "isDoingEarlyGamePush:",
            30,
            () -> {
                if (!GamePhase.isEarlyGame()) {
                    return false;
                }

                if (Enemy.protoss()) {
                    return EnemyUnits.discovered().ofType(AUnitType.Protoss_Zealot).atLeast(6);
                }
                else if (Enemy.terran()) {
                    return EnemyUnits.discovered().ofType(AUnitType.Terran_Marine).atLeast(6);
                }
                else {
                    return EnemyUnits.discovered().ofType(AUnitType.Zerg_Zergling).atLeast(9);
                }
            }
        );
    }

    public static boolean hasHiddenUnits() {
        return EnemyUnits.discovered().ofType(AUnitType.Protoss_Dark_Templar, AUnitType.Zerg_Lurker).notEmpty();
    }

    public static int airUnitsAntiGround() {
        return EnemyUnits.discovered().ofType(AUnitType.Zerg_Mutalisk, AUnitType.Terran_Wraith).count();
    }

    public static int hiddenUnitsCount() {
        return EnemyUnits.discovered().ofType(AUnitType.Protoss_Dark_Templar, AUnitType.Zerg_Lurker).count();
    }

    public static AStrategy strategy() {
        return EnemyStrategy.get();
    }
}
