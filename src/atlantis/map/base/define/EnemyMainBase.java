package atlantis.map.base.define;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;

public class EnemyMainBase {
    private static Cache<APosition> cache = new Cache<>();
    private static APosition _lastNonNull = null;

    public static APosition get() {
        if (_lastNonNull != null) return _lastNonNull;

        return cache.get(
            "get",
            -1,
            EnemyMainBase::refresh
        );
    }

    private static APosition refresh() {
        for (AUnit base : EnemyUnits.discovered().bases().list()) {
            if (BaseLocations.isPositionInStartingLocation(base)) {
                return _lastNonNull = base.position();
            }
        }

        return null;
    }
}
