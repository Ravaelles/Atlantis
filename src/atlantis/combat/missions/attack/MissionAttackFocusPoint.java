package atlantis.combat.missions.attack;

import atlantis.combat.missions.focus.AFocusPoint;
import atlantis.combat.missions.focus.MissionFocusPoint;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.strategy.GamePhase;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AbstractFoggedUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class MissionAttackFocusPoint extends MissionFocusPoint {

    private Cache<AFocusPoint> cache = new Cache<>();

    public AFocusPoint focusPoint() {
        return cache.getIfValid(
                "focusPoint",
                60,
                () -> defineFocusPoint()
        );
    }

    private AFocusPoint defineFocusPoint() {
        if (A.supplyUsed() <= 1 || !Have.main()) {
            AUnit enemy = Select.enemy().first();
            AUnit our = Select.our().first();

            if (our == null) {
                return null;
            }

            if (enemy == null) {
                return null;
            }

            return new AFocusPoint(
                    enemy,
                    our,
                "FirstEnemy"
            );
        }

        // Prevent switching bases across entire map
        if (GamePhase.isEarlyGame() || Select.enemy().buildings().atMost(2)) {

            // Try going near enemy base
            APosition enemyBase = EnemyUnits.enemyBase();
            if (enemyBase != null) {
                return new AFocusPoint(
                        enemyBase,
                        Select.main(),
                    "EnemyBase"
                );
            }
        }

        // Try going near any enemy building
        AbstractFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
        if (enemyBuilding != null && enemyBuilding.position() != null) {
            return new AFocusPoint(
                    enemyBuilding,
                    Select.main(),
                "EnemyBuilding"
            );
        }

        // Try going near any enemy building
        AUnit visibleEnemyBuilding = Select.enemy().buildings().last();
        if (visibleEnemyBuilding != null) {
            return new AFocusPoint(
                    visibleEnemyBuilding,
                    Select.main(),
                "VisibleEnemyBuilding"
            );
        }

        // Try going to any known enemy unit
        HasPosition alphaCenter = Alpha.alphaCenter();
        AUnit anyEnemyUnit = EnemyUnits.discovered().groundUnits().effVisible().nearestTo(
            alphaCenter != null ? alphaCenter : Select.our().first()
        );
//        AUnit anyEnemyUnit = EnemyUnits.visibleAndFogged().combatUnits().groundUnits().first();
        if (anyEnemyUnit != null) {
            return new AFocusPoint(
                    anyEnemyUnit,
                    Select.main(),
                "AnyEnemyUnit"
            );
        }

        if (Count.ourCombatUnits() <= 40) {
            AChoke mainChoke = Chokes.enemyMainChoke();
            if (mainChoke != null) {
                return new AFocusPoint(
                        mainChoke,
                        Select.main(),
                    "EnemyMainChoke"
                );
            }
        }

        // Try to go to some starting location, hoping to find enemy there.
        if (Select.main() != null) {
            APosition startLocation = Bases.nearestUnexploredStartingLocation(Select.main());

            if (startLocation != null) {
                return new AFocusPoint(
                        startLocation,
                        Select.main(),
                    "NearStartLocation"
                );
            }
        }

        return null;
    }

}