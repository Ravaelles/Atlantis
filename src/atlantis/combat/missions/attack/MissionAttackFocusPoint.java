package atlantis.combat.missions.attack;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.MissionFocusPoint;
import atlantis.enemy.EnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.A;
import atlantis.util.Cache;

public class MissionAttackFocusPoint extends MissionFocusPoint {

    private Cache<AFocusPoint> cache = new Cache<>();

    public AFocusPoint focusPoint() {
        return cache.get(
            "focusPoint",
            60,
            () -> {
//                if (A.notUms() && (A.supplyUsed() <= 1 || !Have.main())) {
                if (A.supplyUsed() <= 1 || !Have.main()) {
                    AUnit enemy = Select.enemy().first();
                    AUnit our = Select.our().first();
                    if (enemy == null || our == null) {
                        return null;
                    }

                    return new AFocusPoint(
                            enemy,
                            our
                    );
                }

                // Try going near enemy base
                APosition enemyBase = EnemyUnits.enemyBase();
                if (enemyBase != null) {
                    return new AFocusPoint(
                            enemyBase,
                            Select.main()
                    );
                }

                // Try going near any enemy building
                AFoggedUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
                if (enemyBuilding != null && enemyBuilding.position() != null) {
                    return new AFocusPoint(
                            enemyBuilding,
                            Select.main()
                    );
                }

                // Try going near any enemy building
                AUnit visibleEnemyBuilding = Select.enemy().buildings().last();
                if (visibleEnemyBuilding != null) {
                    return new AFocusPoint(
                            visibleEnemyBuilding,
                            Select.main()
                    );
                }

                // Try going to any known enemy unit
                AUnit anyEnemyUnit = Select.enemy().combatUnits().groundUnits().first();
                if (anyEnemyUnit != null) {
                    return new AFocusPoint(
                            anyEnemyUnit,
                            Select.main()
                    );
                }

                if (Count.ourCombatUnits() <= 40) {
                    AChoke mainChoke = Chokes.enemyMainChoke();
                    if (mainChoke != null) {
                        return new AFocusPoint(
                                mainChoke,
                                Select.main()
                        );
                    }
                }

                // Try to go to some starting location, hoping to find enemy there.
                if (Select.main() != null) {
                    APosition startLocation = Bases.nearestUnexploredStartingLocation(Select.main());

                    if (startLocation != null) {
                        return new AFocusPoint(
                                startLocation,
                                Select.main()
                        );
                    }
                }

                return null;
            }
        );
    }

}