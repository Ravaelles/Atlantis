package atlantis.combat.missions.attack.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyWhoBreachedBase;
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

        AUnit enemyInBase = EnemyWhoBreachedBase.get();
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

        // =========================================================

        if (A.supplyUsed() <= 1) {
            AUnit enemy = Select.enemy().first();

            if (our == null) {
                return null;
            }

            if (enemy == null) {
                return null;
            }

            return new AFocusPoint(
                enemy,
                our,
                "FirstEnemy(" + enemy.name() + ")"
            );
        }

        // === Against early combat buildings ======================

        AFocusPoint focus = containEnemyCombatBuildingsInNaturalChoke(main);
        if (focus != null) return focus;

        // =========================================================

//        AFocusPoint enemyExpansion = enemyExpansion();
//        if (shouldFocusEnemyExpansion(enemyExpansion)) {
//            return enemyExpansion;
//        }

        AFocusPoint enemyThird = enemyThird(main);
        if (shouldFocusEnemyThird(enemyThird)) {
            return enemyThird;
        }

        // =========================================================

        AFocusPoint enemyUnit = nearestCombatUnit(main);
        if (enemyUnit != null) return enemyUnit;

        AFocusPoint enemyCombatBuilding = nearestEnemyCombatBuilding(main);
        if (enemyCombatBuilding != null) return enemyCombatBuilding;

        AFocusPoint enemyBuilding = nearestEnemyBuilding(main);
        if (enemyBuilding != null) return enemyBuilding;

        // Prevent switching bases across entire map
        if (GamePhase.isEarlyGame() || Select.enemy().buildings().atMost(2)) {

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
        Selection enemiesDiscovered = EnemyUnits.discovered().havingWeapon();
        AUnit visibleEnemyBuilding = enemiesDiscovered.buildings().last();
        if (visibleEnemyBuilding != null) {
            return new AFocusPoint(
                visibleEnemyBuilding,
                main,
                "AnyEnemyBuilding(" + visibleEnemyBuilding.name() + ")"
            );
        }

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

        if (Count.ourCombatUnits() <= 40 && EnemyUnits.nearestEnemyBuilding() != null) {
            AChoke mainChoke = Chokes.enemyMainChoke();
            if (mainChoke != null) {
                return new AFocusPoint(
                    mainChoke,
                    main,
                    "EnemyMainChoke"
                );
            }
        }

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

    private AFocusPoint containEnemyCombatBuildingsInNaturalChoke(AUnit main) {
        int enemyCB = EnemyInfo.combatBuildingsAntiLand();
        if (enemyCB <= 0) return null;

        if (A.supplyUsed() >= 140 || A.minerals() >= 1000) return null;
        if (OurArmy.strength() >= 500 && Count.ourCombatUnits() >= 30) return null;

        int ourStrength = Alpha.count();
        if (enemyCB * 8 <= ourStrength) return null;

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

    private boolean shouldFocusEnemyThird(AFocusPoint enemyThird) {
        if (enemyThird == null) return false;
        if (A.s <= 300) return false;
        if (OurArmy.strength() >= 600 && A.seconds() % 30 <= 10) return false;
//        if (EnemyUnits.discovered().combatBuildingsAntiLand().empty() && A.seconds() % 20 <= 9) return null;
        if (!EnemyInfo.hasDefensiveLandBuilding(true)) return false;

        if (
            enemyThird.isPositionVisible()
                && Select.enemy().groundUnits().inRadius(10, enemyThird).empty()
        ) return false;

        boolean periodicallyCheckThird = A.s % 40 <= 10;

        if (EnemyInfo.hasDefensiveLandBuilding(true) || periodicallyCheckThird) {
            if (periodicallyCheckThird && A.supplyUsed() >= 130 && A.supplyUsed() <= 192) return true;
            if (Count.ourCombatUnits() >= 15 && !enemyThird.isExplored()) return true;
        }

        if (Enemy.zerg() && A.s % 36 <= 10) return true;

        HasPosition alphaCenter = Alpha.alphaCenter();
        if (alphaCenter != null && alphaCenter.distTo(enemyThird) >= 20) return true;

        if (
            EnemyInfo.hasNaturalBase()
                && EnemyInfo.hasDefensiveLandBuilding(true)
        ) return true;

        return OurArmy.strength() <= 400 && EnemyInfo.hasDefensiveLandBuilding(true);
    }


    private AFocusPoint enemyThird(AUnit main) {
        APosition enemyThird = BaseLocations.enemyThird();
        if (enemyThird == null) return null;
        if (
            enemyThird.isPositionVisible()
                && (
                Select.enemy().buildings().inRadius(8, enemyThird).empty()
            )
        ) return null;

        if (A.seconds() % 30 <= 12 && EnemyUnits.discovered().combatBuildingsAntiLand().empty()) return null;

        return new AFocusPoint(
            enemyThird,
            Select.mainOrAnyBuilding(),
            "EnemyThird"
        );
    }

    private static AFocusPoint nearestEnemyCombatBuilding(AUnit main) {
        AUnit enemyBuilding = EnemyUnits.nearestEnemyCombatBuilding();
        if (
            enemyBuilding != null
                && enemyBuilding.hasPosition()
//                && (enemyBuilding.isAlive() || !enemyBuilding.isVisibleUnitOnMap())
                && enemyBuilding.isAlive()
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
                && enemyBuilding.isAlive()
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
