package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class EnemyNearBases {
    private static Cache<Object> cache = new Cache<>();
    private static int maxDist;

    public static AUnit enemyNearAnyOurBase(int maxDistToBase) {
        if (maxDistToBase < 0) {
            maxDistToBase = 7;
        }
        maxDist = maxDistToBase;

        AUnit main = Select.main();
        if (main == null) return null;

        AUnit enemyNearMain = main.enemiesNear().combatUnits().havingWeapon().nearestTo(main);
        if (enemyNearMain != null) return enemyNearMain;

        return (AUnit) cache.getIfValid(
            "enemyNearAnyOurBuilding:" + maxDist,
            17,
            () -> {
//                AUnit main = Select.main();

                AUnit nearCritical = Select.enemy().ofType(
                    AUnitType.Protoss_Dark_Templar, AUnitType.Protoss_Reaver, AUnitType.Protoss_Archon,
                    AUnitType.Zerg_Lurker
                ).inRadius(8, Select.ourBuildings()).nearestTo(main);

                if (nearCritical != null) return nearCritical;

                AUnit enemy;
                for (AUnit base : Select.ourBases().list()) {
                    if ((enemy = isNearBase(base)) != null) {
                        return enemy;
                    }
                }

                return null;
            }
        );
    }

    private static AUnit isNearBase(AUnit base) {
        if (base == null) return null;

        AUnit nearestEnemy = potentialRegularEnemies().nearestTo(base);

        return nearestEnemy != null
            && nearestEnemy.distToLessThan(base, maxDist)
            ? nearestEnemy : null;
    }

    private static Selection potentialRegularEnemies() {
        Selection enemies = Select.enemyRealUnits().havingAntiGroundWeapon();

        if (A.supplyUsed() >= 140) {
            enemies = enemies.excludeTypes(
                AUnitType.Protoss_Scout
            );
        }

        return enemies;
    }
}
