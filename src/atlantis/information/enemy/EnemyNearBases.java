package atlantis.information.enemy;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class EnemyNearBases {
    private static Cache<Object> cache = new Cache<>();
    private static Selection enemies;
    private static Selection ourBuildings;
    private static int maxDist;

    public static AUnit enemyNearAnyOurBase(int maxDistToBase) {
        if (maxDistToBase < 0) {
            maxDistToBase = 13;
        }
        maxDist = maxDistToBase;

        return (AUnit) cache.getIfValid(
            "enemyNearAnyOurBuilding:" + maxDist,
            47,
            () -> {
                if (!Have.base()) {
                    return null;
                }

                AUnit nearCritical = Select.enemy().ofType(
                    AUnitType.Protoss_Dark_Templar, AUnitType.Protoss_Reaver, AUnitType.Protoss_Archon,
                    AUnitType.Zerg_Lurker
                ).inRadius(8, Select.ourBuildings()).nearestTo(Select.mainOrAnyBuilding());

                if (nearCritical != null) return nearCritical;

                enemies = Select.enemyCombatUnits().havingWeapon().excludeTypes(
                    AUnitType.Terran_Science_Vessel,
                    AUnitType.Terran_Valkyrie,
                    AUnitType.Protoss_Corsair,
                    AUnitType.Zerg_Scourge
                );
                ourBuildings = Select.ourBuildings();

                AUnit enemy = isNearBase(Select.main());
                if (enemy != null) {
                    return enemy;
                }

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

        AUnit nearestEnemy;

        // === Crucial ============================================

        nearestEnemy = enemies.crucialUnits().nearestTo(base);

        if (nearestEnemy != null) {
            if (nearestEnemy.distToLessThan(base, maxDist) && base.regionsMatch(nearestEnemy)) {
                if (ourBuildings.nearestTo(nearestEnemy).distTo(nearestEnemy) < 10) {
                    return nearestEnemy;
                }
            }
        }

        // === Regular enemies =====================================

        nearestEnemy = enemies.nearestTo(base);

        if (nearestEnemy != null) {
            if (nearestEnemy.distToLessThan(base, maxDist) && base.regionsMatch(nearestEnemy)) {
                if (ourBuildings.nearestTo(nearestEnemy).distTo(nearestEnemy) < 7.5) {
                    return nearestEnemy;
                }
            }
        }

        // =========================================================

        return null;
    }
}
