package atlantis.map.base.define;

import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;

public class EnemyNaturalBase {
    private static Cache<APosition> cache = new Cache<>();

    public static APosition get() {
        return cache.getIfValid(
            "get",
            -1,
            () -> {
                HasPosition enemyMain = BaseLocations.enemyMain();
                if (enemyMain == null) {
                    return null;
                }

                ABaseLocation baseLocation = DefineNaturalBase.naturalIfMainIsAt(enemyMain);
                if (baseLocation != null) {
                    return baseLocation.position();
                }

                return null;
            }
        );
    }
}
