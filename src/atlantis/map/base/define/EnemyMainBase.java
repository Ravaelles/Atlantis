package atlantis.map.base.define;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;

public class EnemyMainBase {
    private static Cache<APosition> cache = new Cache<>();

    public static APosition get() {
        return cache.getIfValid(
            "get",
            271,
            () -> {
                for (AUnit base : EnemyUnits.discovered().bases().list()) {
                    if (BaseLocations.isPositionInStartingLocation(base)) {
                        return base.position();
                    }
                }

                return null;
            }
        );
    }
}
