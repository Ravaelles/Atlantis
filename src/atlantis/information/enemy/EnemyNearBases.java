package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class EnemyNearBases {
    public static final int DIST_CONSIDERED_CLOSE_TO_BASE = 8;
    private static Cache<Object> cache = new Cache<>();
    private static int maxDist;

    public static AUnit enemyNearAnyOurBase(int maxDistToBase) {
        if (Count.bases() == 0) return null;

        if (maxDistToBase < 0) {
            maxDistToBase = DIST_CONSIDERED_CLOSE_TO_BASE;
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
                ).visibleOnMap().inRadius(8, Select.ourBuildings()).nearestTo(main);

                if (nearCritical != null) return nearCritical;

                AUnit enemy;
                for (AUnit base : Select.ourBases().list()) {
                    if ((enemy = isNearBase(base)) != null && verifyEnemy(enemy)) {
                        return enemy;
                    }
                }

                return null;
            }
        );
    }

    private static boolean verifyEnemy(AUnit enemy) {
        if (enemy.isZergling() && enemy.friendsNear().countInRadius(8, enemy) == 0) return false;

        return true;
    }

    private static AUnit isNearBase(AUnit base) {
        if (base == null) return null;

        AUnit nearestEnemy = potentialRegularEnemies().nearestTo(base);

        return nearestEnemy != null
            && nearestEnemy.groundDist(base) <= maxDist
            ? nearestEnemy : null;
    }

    private static Selection potentialRegularEnemies() {
        Selection enemies = Select.enemyRealUnits().combatUnits().havingAntiGroundWeapon();

        if (A.supplyUsed() >= 140) {
            enemies = enemies.excludeTypes(
                AUnitType.Protoss_Scout
            );
        }

        return enemies;
    }
}
