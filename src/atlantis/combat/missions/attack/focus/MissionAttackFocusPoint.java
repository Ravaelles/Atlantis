package atlantis.combat.missions.attack.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyNearBases;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.OurArmy;
import atlantis.information.strategy.GamePhase;
import atlantis.map.AMap;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class MissionAttackFocusPoint extends MissionFocusPoint {

    private Cache<AFocusPoint> cache = new Cache<>();

    private APosition _temporaryTarget = null;
    private AUnit main;
    private AUnit our;

    public AFocusPoint focusPoint() {
        return cache.getIfValid(
            "focusPoint",
            61,
            () -> defineFocusPoint()
        );
    }

    private AFocusPoint defineFocusPoint() {
        main = Select.main();
        our = Select.our().first();

        // === AliveEnemies that breached into base =====================

        AFocusPoint enemyInBase = enemyWhoBreachedBase();
        if (enemyInBase != null) return enemyInBase;

        // === Enemy near base ===========================================

        if (A.supplyUsed() <= 90) {
            AUnit enemyNearBase = EnemyNearBases.enemyNearAnyOurBase(8);
            if (
                enemyNearBase != null
                    && (enemyNearBase.friendsInRadiusCount(7) <= 1 || enemyNearBase.isCrucialUnit() || !enemyNearBase.effUndetected())
            ) {
                return new AFocusPoint(
                    enemyNearBase,
                    "EnemyNearBase"
                );
            }
        }

        // === Third ===============================================

        AttackEnemyThird enemyThird = new AttackEnemyThird();
        if (enemyThird.shouldFocusIt()) {
            return enemyThird.enemyThird();
        }

        // === Expansions ===============================================

        AttackEnemyExpansion enemyExpansion = new AttackEnemyExpansion();
        if (enemyExpansion.shouldFocusIt()) {
            return enemyExpansion.expansion();
        }

        // === Against early combat buildings ======================

        AFocusPoint focus = containEnemyCombatBuildingsInNaturalChoke(main);
        if (focus != null) return focus;

        // =========================================================

        AFocusPoint enemyUnit = nearestCombatUnit(main);
        if (enemyUnit != null) return enemyUnit;

        AFocusPoint enemyCombatBuilding = nearestEnemyCombatBuilding(main);
        if (enemyCombatBuilding != null) return enemyCombatBuilding;

        AFocusPoint enemyBuilding = nearestEnemyBuilding(main);
        if (enemyBuilding != null) return enemyBuilding;

        // Prevent switching bases across entire map
        if (GamePhase.isEarlyGame()) {

            // Try going near enemy base
            AUnit enemyBase = EnemyUnits.enemyBase();
            if (enemyBase != null) {
                return new AFocusPoint(
                    enemyBase,
                    main,
                    "EnemyBase(" + enemyBase.name() + ")"
                );
            }
        }

        // Try going near any enemy building
//        AUnit visibleEnemyBuilding = enemiesDiscovered.buildings().last();
//        if (visibleEnemyBuilding != null) {
//            return new AFocusPoint(
//                visibleEnemyBuilding,
//                main,
//                "AnyEnemyBuilding(" + visibleEnemyBuilding.name() + ")"
//            );
//        }

        Selection enemiesDiscovered = EnemyUnits.discovered().havingWeapon();

        // Try going to any known enemy unit
        HasPosition alphaCenter = Alpha.alphaCenter();
        AUnit anyEnemyLandUnit = enemiesDiscovered.groundUnits().effVisible().realUnits().nearestTo(
            alphaCenter != null ? alphaCenter : Select.our().first()
        );
//        AUnit anyEnemyLandUnit = EnemyUnits.visibleAndFogged().combatUnits().groundUnits().first();
        if (anyEnemyLandUnit != null) {
            return new AFocusPoint(
                anyEnemyLandUnit,
                main,
                "AnyEnemyLandUnit(" + anyEnemyLandUnit.name() + ")"
            );
        }

        AUnit anyEnemyAirUnit = enemiesDiscovered.air().effVisible().nearestTo(
            alphaCenter != null ? alphaCenter : Select.our().first()
        );
        if (anyEnemyAirUnit != null) {
            return new AFocusPoint(
                anyEnemyAirUnit,
                main,
                "AnyEnemyAirUnit(" + anyEnemyAirUnit.name() + ")"
            );
        }

//        if (Count.ourCombatUnits() <= 40 && EnemyUnits.nearestEnemyBuilding() != null) {
//            AChoke mainChoke = Chokes.enemyMainChoke();
//            if (mainChoke != null) {
//                return new AFocusPoint(
//                    mainChoke,
//                    main,
//                    "EnemyMainChoke"
//                );
//            }
//        }

        // Try to go to some starting location, hoping to find enemy there.
        if (main != null) {
            APosition startLocation = BaseLocations.nearestUnexploredStartingLocation(main);

//            System.err.println("startLocation = " + startLocation);
//            if (startLocation != null && startLocation.isExplored()) {
//                System.err.println("Damn, this start location is already explored!");
//                System.err.println(startLocation.isExplored() + " / " + startLocation.isPositionVisible());
//            }

            if (startLocation != null) {
                return new AFocusPoint(
                    startLocation,
                    main,
                    "UnexplStartLoc"
                );
            }
        }

        if (isTemporaryTargetStillValid()) {
            return new AFocusPoint(
                _temporaryTarget,
                our,
                "RandPosition"
            );
        }

        if (our == null) our = Select.all().first();

        // Go to random UNEXPLORED
        _temporaryTarget = AMap.randomUnexploredPosition(our);
        if (_temporaryTarget != null && _temporaryTarget.hasPathTo(our.position())) {
            return new AFocusPoint(
                _temporaryTarget,
                our,
                "RandomUnexplored"
            );
        }

        // Go to random INVISIBLE
        for (int i = 0; i < 14; i++) {
            _temporaryTarget = AMap.randomInvisiblePosition(our);
            if (_temporaryTarget != null && _temporaryTarget.hasPathTo(our.position())) {
                return new AFocusPoint(
                    _temporaryTarget,
                    our,
                    "RandomInvisible"
                );
            }
        }

        if (!A.isUms() && EnemyUnits.discovered().count() >= 1) {
            ErrorLog.printMaxOncePerMinute("No MissionAttack FocusPoint :-|");
        }

        return null;
    }

    private static AFocusPoint enemyWhoBreachedBase() {
        if (A.supplyUsed() <= 160) {
            AUnit enemyInBase = EnemyUnitBreachedBase.get();
            if (
                enemyInBase != null
                    && (enemyInBase.friendsInRadiusCount(5) >= 1 || enemyInBase.isCrucialUnit())
                    && !enemyInBase.effUndetected()
            ) {
                return new AFocusPoint(
                    enemyInBase,
                    "EnemyBreachedBase"
                );
            }
        }
        return null;
    }

    private AFocusPoint containEnemyCombatBuildingsInNaturalChoke(AUnit main) {
        if (!Enemy.terran()) return null;
        if (A.supplyUsed() >= 140 || A.minerals() >= 1000) return null;

        int enemyCB = EnemyInfo.combatBuildingsAntiLand();
        if (enemyCB <= 0) return null;

        if (OurArmy.strength() >= 500 && Count.ourCombatUnits() >= 30) return null;

        int ourCount = Alpha.count();
        if (ourCount >= 18 || enemyCB * 8 <= ourCount) return null;

        AChoke choke = Chokes.enemyNaturalChoke();
        if (choke == null) return null;

//        choke = Chokes.enemyMainChoke();
//        if (choke == null) return null;

        HasPosition chokeMoved = choke.groundTranslateTowardsMain(4);

        return new AFocusPoint(
            (chokeMoved != null ? chokeMoved : choke),
            main,
            "ContainCB(" + enemyCB + ")"
        );
    }

    private static AFocusPoint nearestEnemyCombatBuilding(AUnit main) {
        AUnit enemyBuilding = EnemyUnits.nearestEnemyCombatBuilding();
        if (
            enemyBuilding != null
                && enemyBuilding.hasPosition()
//                && (enemyBuilding.isAlive() || !enemyBuilding.isVisibleUnitOnMap())
                && enemyBuilding.hp() >= 1
        ) {
            return new AFocusPoint(
                enemyBuilding,
                main,
                "EnemyCB(" + enemyBuilding.name() + ")"
            );
        }
        return null;
    }

    private static AFocusPoint nearestEnemyBuilding(AUnit main) {
        AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
        if (
            enemyBuilding != null
                && enemyBuilding.hasPosition()
//                && (enemyBuilding.isAlive() || !enemyBuilding.isVisibleUnitOnMap())
                && enemyBuilding.hp() >= 1
        ) {
            return new AFocusPoint(
                enemyBuilding,
                main,
                "EnemyBuilding(" + enemyBuilding.name() + ")"
            );
        }
        return null;
    }

    private static AFocusPoint nearestCombatUnit(AUnit main) {
//        AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
//        if (enemyBuilding == null) return null;

        AUnit nearestTo = Alpha.get().leader();
        if (nearestTo == null) nearestTo = main;

        AUnit enemy = Select.enemy().combatUnits().effVisible().nearestTo(nearestTo);
        if (enemy == null) return null;

        if (enemy.hp() <= 0) return null;

        return new AFocusPoint(
            enemy,
            main,
            "EnemyUnit(" + enemy.name() + ")"
        );
    }

    private boolean isTemporaryTargetStillValid() {
        return _temporaryTarget != null && !_temporaryTarget.isPositionVisible();
    }

}
