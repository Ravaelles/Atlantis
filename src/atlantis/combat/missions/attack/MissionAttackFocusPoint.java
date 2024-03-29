package atlantis.combat.missions.attack;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MissionFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.map.choke.AChoke;
import atlantis.map.AMap;
import atlantis.map.base.Bases;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class MissionAttackFocusPoint extends MissionFocusPoint {

    private Cache<AFocusPoint> cache = new Cache<>();

    private APosition _temporaryTarget = null;

    public AFocusPoint focusPoint() {
        return cache.getIfValid(
            "focusPoint",
            61,
            () -> defineFocusPoint()
        );
    }

    private AFocusPoint defineFocusPoint() {
        AUnit our = Select.our().first();
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

        // Try going near any enemy building
        AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
        AUnit main = Select.main();
        if (
            enemyBuilding != null
                && enemyBuilding.position() != null
                && (enemyBuilding.isAlive() || !enemyBuilding.isVisibleUnitOnMap())
        ) {
            return new AFocusPoint(
                enemyBuilding,
                main,
                "EnemyBuilding(" + enemyBuilding.name() + ")"
            );
        }

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
        AUnit visibleEnemyBuilding = EnemyUnits.discovered().buildings().last();
        if (visibleEnemyBuilding != null) {
            return new AFocusPoint(
                visibleEnemyBuilding,
                main,
                "AnyEnemyBuilding(" + visibleEnemyBuilding.name() + ")"
            );
        }

        // Try going to any known enemy unit
        HasPosition alphaCenter = Alpha.alphaCenter();
        AUnit anyEnemyLandUnit = EnemyUnits.discovered().groundUnits().effVisible().realUnits().nearestTo(
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

        AUnit anyEnemyAirUnit = EnemyUnits.discovered().air().effVisible().nearestTo(
            alphaCenter != null ? alphaCenter : Select.our().first()
        );
        if (anyEnemyAirUnit != null) {
            return new AFocusPoint(
                anyEnemyAirUnit,
                main,
                "AnyEnemyAirUnit(" + anyEnemyAirUnit.name() + ")"
            );
        }

        if (Count.ourCombatUnits() <= 40) {
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
            APosition startLocation = Bases.nearestUnexploredStartingLocation(main);

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

//        System.out.println("New temp target @ " + A.now());

        if (our == null) {
            return null;
        }

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
        _temporaryTarget = AMap.randomInvisiblePosition(our);
        if (_temporaryTarget != null && _temporaryTarget.hasPathTo(our.position())) {
            return new AFocusPoint(
                _temporaryTarget,
                our,
                "RandomInvisible"
            );
        }

//        System.err.println("No MissionAttack FocusPoint");
        return null;
    }

    private boolean isTemporaryTargetStillValid() {
        return _temporaryTarget != null && !_temporaryTarget.isPositionVisible();
    }

}
