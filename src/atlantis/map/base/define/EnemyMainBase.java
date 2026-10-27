package atlantis.map.base.define;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

/**
 * Where the enemy started, once anybody has seen it.
 *
 * <p>A miss is never cached: the first {@code get()} of a game runs while
 * nothing is discovered yet (combat focus reads this from the early frames),
 * and caching that null forever is exactly what kept ten live games at
 * 0/5111 known-enemy scout rows - the scout later stood on the enemy
 * Hatchery while every reader kept the early null (B-43, measured
 * 20261027_183253 through 20261028_004959, all V2 LIVE TauCross). Only the
 * hit latches ({@code _lastNonNull}); misses re-run the discovery scan,
 * which itself rides the already 1-frame-cached {@code discovered()}.</p>
 */
public class EnemyMainBase {

    private static APosition _lastNonNull = null;

    public static APosition get() {
        if (_lastNonNull != null) return _lastNonNull;

        for (AUnit base : EnemyUnits.discovered().bases().list()) {
            if (BaseLocations.isPositionInStartingLocation(base)) {
                return _lastNonNull = base.position();
            }
        }

        return null;
    }

    /**
     * Per-game reset (see {@code OnGameStarted}): a latched main must not
     * leak into the next game in one JVM.
     */
    public static void clear() {
        _lastNonNull = null;
    }
}
