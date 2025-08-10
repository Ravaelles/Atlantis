package atlantis.combat.missions.attack.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.combat.advance.focus_choke.MiddleFocusChoke;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyNearBases;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.map.AMap;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;
import atlantis.production.dynamic.protoss.tech.ResearchSingularityCharge;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AliveEnemies;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;
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
            53,
            () -> defineFocusPoint()
        );
    }

    private AFocusPoint defineFocusPoint() {
        main = Select.mainOrAnyBuilding();
        our = Select.our().first();

        AFocusPoint focus = defendLikePositions();
        if (focus != null) return focus;

        focus = enemiesNearMain();
        if (focus != null) return focus;

        if (A.supplyUsed() >= 90 || Alpha.count() >= 14 || Army.strength() >= 180 || EnemyUnits.combatUnits() <= 10) {
            focus = enemyExpansionsPositions();
            if (focus != null) return focus;
        }

        focus = middleMapChokePoint();
        if (focus != null) return focus;

        focus = enemyExpansionsPositions();
        if (focus != null) return focus;

        focus = enemyNearAlpha();
        if (focus != null) return focus;

        focus = enemyAnyUnitsPositions();
        if (focus != null) return focus;

        focus = enemyBuildingPositions();
        if (focus != null) return focus;

//        focus = enemyMainChoke();
//        if (focus != null) return focus;

        focus = guessPositions();
        if (focus != null) return focus;

        return null;
    }

    private AFocusPoint enemiesNearMain() {
        if (A.supplyUsed() >= 70) return null;

        AUnit main = Select.mainOrAnyBuilding();
        if (main == null) return null;

        if (Enemy.zerg()) {
            AUnit muta = Select.enemies(AUnitType.Zerg_Mutalisk).nearestTo(main);
            if (muta != null && muta.enemiesNear().buildings().countInRadius(8, muta) > 0) {
                return new AFocusPoint(
                    muta,
                    "EnemyMuta!"
                );
            }
        }

        Selection visibleEnemies = Select.enemyCombatUnits().visibleOnMap().inGroundRadius(25, main);
        if (visibleEnemies.count() <= 2) return null;

        AUnit enemyNearestToMain = visibleEnemies.nearestTo(main);
        if (enemyNearestToMain == null) return null;

        if (enemyNearestToMain.friendsNear().atMost(1)) return null;

        return new AFocusPoint(
            enemyNearestToMain,
            "EnemiesNearMain"
        );
    }

    private AFocusPoint enemyNearAlpha() {
        HasPosition alpha = Alpha.alphaCenter();
        if (alpha == null) return null;

        AUnit enemy = AliveEnemies.get().combatUnits().groundUnits().inRadius(20, alpha).nearestTo(alpha);
        if (enemy == null) return null;

        if (enemy.friendsNear().combatBuildingsAntiLand().countInRadius(12, enemy) == 0) {
            return new AFocusPoint(
                enemy,
                main,
                "EnemyNearAlpha"
            );
        }

        return null;
    }

    private AFocusPoint middleMapChokePoint() {
        if (!Enemy.zerg()) return null;

        if (A.supplyUsed() >= 160 || A.hasMinerals(1000)) return null;

//        if (
//            goons >= 12
//                && !ResearchSingularityCharge.isResearched()
//                && Army.strengthWithoutCB() <= 125
//        ) {
//            if (DEBUG) reason = "Goons (" + goons + ") and no goon range(" + Army.strength() + "%)";
//            return forceMissionSpartaOrDefend(reason);
//        }

        if (Enemy.zerg()) {
            if (!EnemyExistingExpansion.found()) {
                if (
                    (Army.strengthWithoutCB() >= 150 || Alpha.count() >= 16)
                        && ResearchSingularityCharge.isResearched()
        //                && EnemyUnits.ranged() >= 5
                        && Count.dragoons() >= 8
                ) return null;
            }
        }

//        AChoke choke = CurrentFocusChoke.get();
        AChoke choke = MiddleFocusChoke.get();
        if (choke != null) {
//            System.err.println("FocusChoke = " + choke + " / strength:" + Army.strength());
            return new AFocusPoint(
                choke,
                main,
                "MiddleFocusChoke"
            );
        }

        return null;
    }

    private AFocusPoint enemyMainChoke() {
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
        return null;
    }

    private AFocusPoint enemyExpansionsPositions() {
        // === Third ===============================================

//        AttackEnemyThird enemyThird = new AttackEnemyThird();
//        if (enemyThird.shouldFocusIt()) {
//            return enemyThird.enemyThird();
//        }

        // === Expansions ===============================================

        AttackEnemyExpansion enemyExpansion = new AttackEnemyExpansion();
        if (enemyExpansion.shouldFocusIt()) {
            return enemyExpansion.expansion();
        }

        // =========================================================

        return null;
    }

    private AFocusPoint enemyBuildingPositions() {
        AFocusPoint enemyCombatBuilding = nearestEnemyCombatBuilding(main);
        if (enemyCombatBuilding != null) return enemyCombatBuilding;

        AFocusPoint enemyBuilding = nearestEnemyBuilding(main);
        if (enemyBuilding != null) return enemyBuilding;

//        // Prevent switching bases across entire map
//        if (GamePhase.isEarlyGame()) {

        // Try going near enemy base
        AUnit enemyBase = EnemyUnits.enemyBase();
        if (enemyBase != null) {
            return new AFocusPoint(
                enemyBase.position(),
                main,
                "EnemyBase(" + enemyBase.name() + ")"
            );
        }

        return null;
    }

    private AFocusPoint enemyAnyUnitsPositions() {
        AFocusPoint enemyUnit = nearestCombatUnit(main);
        if (enemyUnit != null) return enemyUnit;

//        }

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
        return null;
    }

    private AFocusPoint guessPositions() {
        APosition startLocation = BaseLocations.nearestUnexploredStartingLocation(main);

        if (startLocation != null) {
            return new AFocusPoint(
                startLocation.position(),
                main,
                "UnexplStartLoc"
            );
        }

        // Try to go to some starting location, hoping to find enemy there.
        if (A.s % 50 <= 33) {
            startLocation = BaseLocations.nearestInvisibleStartingLocation(main);

            if (startLocation != null) {
                return new AFocusPoint(
                    startLocation.position(),
                    main,
                    "InvisStartLoc"
                );
            }
        }

        else if (isTemporaryTargetStillValid()) {
            return new AFocusPoint(
                _temporaryTarget.position(),
                our,
                "RandPosition"
            );
        }

        if (our == null) our = Select.all().first();

        // Go to random UNEXPLORED
        _temporaryTarget = AMap.randomUnexploredPosition(our);
        if (
            _temporaryTarget != null
                && _temporaryTarget.hasPosition()
                && !_temporaryTarget.isPositionVisible()
                && _temporaryTarget.hasPathTo(our.position())
        ) {
            return new AFocusPoint(
                _temporaryTarget.position(),
                our,
                "RandomUnexplored"
            );
        }

//        // Go to random INVISIBLE
//        for (int i = 0; i < 14; i++) {
//            _temporaryTarget = AMap.randomInvisiblePosition(our);
//            if (_temporaryTarget != null && _temporaryTarget.hasPosition() && _temporaryTarget.hasPathTo(our.position())) {
//                return new AFocusPoint(
//                    _temporaryTarget,
//                    our,
//                    "RandomInvisible"
//                );
//            }
//        }

        if (!A.isUms() && EnemyUnits.discovered().count() >= 1) {
            ErrorLog.printMaxOncePerMinute("No MissionAttack FocusPoint :-|");
        }
        return null;
    }

    private AFocusPoint defendLikePositions() {
        AUnit main = Select.mainOrAnyBuilding();
        if (main == null) return null;

        // === AliveEnemies that breached into base =====================

        AFocusPoint enemyInBase = enemyWhoBreachedBase();
        if (enemyInBase != null) return enemyInBase;

        // === Enemy near base ===========================================

        AFocusPoint enemyVeryCloseToAnyBase = enemyVeryCloseToAnyBase();
        if (enemyVeryCloseToAnyBase != null) return enemyVeryCloseToAnyBase;

        AFocusPoint enemyNearestToBase = enemyNearestToBase(main);
        if (enemyNearestToBase != null) return enemyNearestToBase;

        // === Against early combat buildings ======================

        AFocusPoint focus = containEnemyCombatBuildingsInNaturalChoke(main);
        if (focus != null) return focus;

        return null;
    }

    private static AFocusPoint enemyVeryCloseToAnyBase() {
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
        return null;
    }

    private static AFocusPoint enemyNearestToBase(AUnit main) {
        if (A.supplyUsed() <= 50) {
            AUnit closeEnemy = EnemyUnits.discovered()
                .combatUnits()
                .groundUnits()
                .inGroundRadius(40, main)
                .groundNearestTo(main);
            if (closeEnemy != null) return new AFocusPoint(
                closeEnemy,
                "EnemyCloseToMain"
            );
        }
        return null;
    }

    private static AFocusPoint enemyWhoBreachedBase() {
        if (A.supplyUsed() <= 160 && !A.hasMinerals(2000)) {
            AUnit enemyInBase = EnemyUnitBreachedBase.get();
            if (
                enemyInBase != null
                    && enemyInBase.hasPosition()
                    && enemyInBase.isVisibleUnitOnMap()
                    && enemyInBase.hp() > 0
                    && (enemyInBase.friendsInRadiusCount(5) >= 1 || enemyInBase.isCrucialUnit())
                    && !enemyInBase.effUndetected()
            ) {
                return new AFocusPoint(
                    enemyInBase,
                    "EnemyBreachedBase_A"
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

        if (Army.strength() >= 400 && Count.ourCombatUnits() >= 6) return null;

        int ourCount = Alpha.count();
        if (ourCount >= 18 || enemyCB * 8 <= ourCount) return null;

        AChoke choke = Chokes.enemyNaturalChoke();
        if (choke == null) return null;

//        choke = Chokes.enemyMainChoke();
//        if (choke == null) return null;

        HasPosition chokeMoved = choke.groundTranslateTowardsMain(4);

        ARegion region = chokeMoved.position().region();
        if (region != null && region.center() != null) {
            chokeMoved = choke.translateTilesTowards(6, region.center());
        }

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
                enemyBuilding.position(),
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
//                && enemyBuilding.hp() >= 1
        ) {
            return new AFocusPoint(
                enemyBuilding.position(),
                main,
                "EnemyBuilding(" + enemyBuilding.name() + ")"
            );
        }
        return null;
    }

    private static AFocusPoint nearestCombatUnit(AUnit main) {
//        AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
//        if (enemyBuilding == null) return null;

//        AUnit nearestTo = Alpha.get().leader();
        AUnit nearestTo = Select.mainOrAnyBuilding();

        Selection enemies = Select.enemy().combatUnits().visibleOnMap().effVisible().havingPosition();
        AUnit enemy = enemies.ranged().groundNearestTo(nearestTo);
        if (enemy == null) enemies.groundNearestTo(nearestTo);
        if (enemy == null) return null;

        if (enemy.hp() <= 0) return null;
        if (!enemy.hasPosition()) return null;
        if (!enemy.isVisibleUnitOnMap()) return null;

        return new AFocusPoint(
            enemy,
            main,
            "EnemyUnit(" + enemy.name() + ")"
        );
    }

    private boolean isTemporaryTargetStillValid() {
        return _temporaryTarget != null
            && _temporaryTarget.hasPosition()
            && !_temporaryTarget.isPositionVisible()
            && Select.our().first().hasPathTo(_temporaryTarget);
    }

}
