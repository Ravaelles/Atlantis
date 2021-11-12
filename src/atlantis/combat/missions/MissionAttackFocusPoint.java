package atlantis.combat.missions;

import atlantis.enemy.AEnemyUnits;
import atlantis.information.AFoggedUnit;
import atlantis.map.AChoke;
import atlantis.map.Bases;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.Cache;

public class MissionAttackFocusPoint extends MissionFocusPoint {

    private Cache<APosition> cache = new Cache<>();

    public APosition focusPoint() {
        return cache.get(
            "focusPoint",
            60,
            () -> {
                // Try going near enemy base
                APosition enemyBase = AEnemyUnits.enemyBase();
                if (enemyBase != null) {
                    return enemyBase;
                }

                // Try going near any enemy building
                AFoggedUnit enemyBuilding = AEnemyUnits.nearestEnemyBuilding();
                if (enemyBuilding != null && enemyBuilding.position() != null) {
                    return enemyBuilding.position();
                }

                // Try going near any enemy building
                AUnit anEnemyBuilding = Select.enemy().buildings().last();
                if (anEnemyBuilding != null) {
                    return anEnemyBuilding.position();
                }

                // Try going to any known enemy unit
                AUnit anyEnemyUnit = Select.enemy().combatUnits().groundUnits().first();
                if (anyEnemyUnit != null) {
                    return anyEnemyUnit.position();
                }

                if (Count.ourCombatUnits() <= 40) {
                    AChoke mainChoke = Chokes.enemyMainChoke();
                    if (mainChoke != null) {
                        return mainChoke.position();
                    }
                }

                // Try to go to some starting location, hoping to find enemy there.
                if (Select.main() != null) {
                    APosition startLocation = Bases.nearestUnexploredStartingLocation(Select.main());

                    if (startLocation != null) {
                        return startLocation;
                    }
                }

                return null;
            }
        );
    }

}