package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.information.strategy.GamePhase;
import atlantis.map.base.ABaseLocation;
import atlantis.map.choke.AChoke;
import atlantis.map.AMap;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;
import atlantis.util.cache.Cache;

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
        return EnemyNearBases.enemyNearAnyOurBase(-1) != null;
    }

    /**
     * Returns <b>true</b> if we have discovered at least one enemy building <b>(and it's still alive)</b>.
     */
    public static boolean hasDiscoveredAnyBuilding() {
        return cacheBoolean.get(
            "hasDiscoveredAnyBuilding",
            53,
            () -> {
                for (AUnit enemyUnit : EnemyUnits.discovered().list()) {
                    if (enemyUnit.isABuilding() && !UnitsArchive.isDestroyed(enemyUnit)) {
                        return true;
                    }
                }
                return false;
            }
        );
    }

    public static int combatBuildingsAntiLand() {
        return (int) cache.get(
            "hasDiscoveredAnyBuilding",
            53,
            () -> EnemyUnits.discovered().combatBuildingsAntiLand().size()
        );
    }

    /**
     * Returns <b>true</b> if we have discovered at least one enemy building <b>(and it's still alive)</b>.
     */
    public static boolean weKnowAboutAnyCombatUnit() {
        return cacheBoolean.get(
            "weKnowAboutAnyCombatUnit",
            33,
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

    /**
     * Returns <b>true</b> if we have discovered at least one enemy building <b>(and it's still alive)</b>.
     */
    public static boolean weKnowAboutAnyRealUnit() {
        return cacheBoolean.get(
            "weKnowAboutAnyRealUnit",
            32,
            () -> {
                for (AUnit enemyUnit : EnemyUnits.discovered().realUnits().list()) {
                    if (!UnitsArchive.isDestroyed(enemyUnit)) {
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

                APosition position = BaseLocations.nearestUnexploredStartingLocation(Select.our().first());
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
            33,
            () -> {
                Selection selection = EnemyUnits.discovered()
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

        return Select.enemyCombatUnits().havingAntiGroundWeapon().inRadius(15, main).nearestTo(main);
    }

    public static boolean isProxyBuilding(AUnit enemyBuilding) {
        if (A.seconds() >= 400 || !We.haveBase()) return false;

        return Select.main().distToLessThan(enemyBuilding, 20);
    }

    public static boolean isDoingEarlyGamePush() {
        return cacheBoolean.get(
            "isDoingEarlyGamePush:",
            30,
            () -> {
                if (!GamePhase.isEarlyGame()) return false;

                if (
                    (A.supplyUsed() >= 14 && EnemyStrategy.get().isUnknown())
                        || (A.supplyUsed() <= 30 && ArmyStrength.weAreMuchWeaker())
                ) return true;

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

    public static boolean hasRanged() {
        return EnemyUnits.discovered().ranged().notEmpty();
    }

    public static boolean noRanged() {
        return EnemyUnits.discovered().ranged().empty();
    }

    public static boolean hasNaturalBase() {
        APosition enemyNatural = BaseLocations.enemyNatural();
        if (enemyNatural == null) return false;

        return EnemyUnits.buildings().inRadius(5, enemyNatural).atLeast(1);
    }

    public static APosition enemyMain() {
        return (APosition) cache.getIfValid(
            "enemyMain",
            271,
            () -> {
                Positions<ABaseLocation> startingLocations = new Positions<>(BaseLocations.startingLocations(true));

                for (AUnit enemyBase : EnemyUnits.discovered().bases().list()) {
                    ABaseLocation location = startingLocations.nearestTo(enemyBase);
                    if (location != null && location.distTo(enemyBase) <= 10) return location.position();
                }

                return null;
            }
        );
    }

    public static APosition enemyNatural() {
        return BaseLocations.enemyNatural();
    }

    public static boolean goesTemplarArchives() {
        return EnemyUnits.discovered().ofType(AUnitType.Protoss_Templar_Archives).notEmpty()
            || (Count.ourCombatUnits() <= 10 && EnemyUnits.discovered().ofType(AUnitType.Protoss_Citadel_of_Adun).notEmpty());
    }

    public static double ourCombatUnitsToEnemyRatio() {
        return (double) Count.ourCombatUnits() / EnemyUnits.combatUnits();
    }
}
