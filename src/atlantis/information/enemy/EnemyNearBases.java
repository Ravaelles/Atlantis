package atlantis.information.enemy;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

public class EnemyNearBases {
    private static Cache<Object> cache = new Cache<>();

    public static AUnit enemyNearAnyOurBase(int maxDistToBase) {
        if (maxDistToBase < 0) {
            maxDistToBase = 12;
        }
        int finalMaxDistToBase = maxDistToBase;

        return (AUnit) cache.getIfValid(
            "enemyNearAnyOurBuilding:" + maxDistToBase,
            47,
            () -> {
                if (!Have.base()) {
                    return null;
                }

                Selection enemies = Select.enemyCombatUnits().havingWeapon().excludeTypes(
                    AUnitType.Terran_Medic,
                    AUnitType.Terran_Science_Vessel,
                    AUnitType.Terran_Valkyrie,
                    AUnitType.Protoss_Observer,
                    AUnitType.Protoss_Corsair,
                    AUnitType.Zerg_Overlord,
                    AUnitType.Zerg_Scourge
                );
                Selection ourBuildings = Select.ourBuildings();

                for (AUnit base : Select.ourBases().list()) {
                    AUnit nearestEnemy = enemies.nearestTo(base);
                    if (nearestEnemy != null) {
                        if (nearestEnemy.distToLessThan(base, finalMaxDistToBase)) {
                            return nearestEnemy;
                        }

                        if (ourBuildings.nearestTo(nearestEnemy).distTo(nearestEnemy) < 6) {
                            return nearestEnemy;
                        }
                    }
                }

                return null;
            }
        );
    }
}
