package atlantis.map.base.define;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.util.cache.Cache;

/**
 * The enemy's fourth base: the closest base location past the natural and
 * the third. Only used as the last resort of the scout fallback chain -
 * by the time it matters the game is long and the earlier anchors are
 * explored.
 */
public class EnemyFourthBase {
    private static final Cache<APosition> cache = new Cache<>();

    public static APosition position() {
        return cache.getIfValid(
            "get",
            30 * 23,
            () -> {
                HasPosition enemyMain = BaseLocations.enemyMain();
                if (enemyMain == null) return null;

                APosition enemyNatural = BaseLocations.enemyNatural();
                if (enemyNatural == null) return null;

                APosition enemyThird = EnemyThirdBase.position();

                double bestDist = 9999;
                ABaseLocation bestBase = null;

                for (ABaseLocation baseLocation : BaseLocations.baseLocations()) {
                    if (baseLocation.isStartLocation()) continue;

                    double distToMain = enemyMain.groundDist(baseLocation);
                    if (distToMain < 15) continue;
                    if (enemyNatural.groundDist(baseLocation) < 5) continue;
                    if (enemyThird != null && enemyThird.groundDist(baseLocation) < 5) continue;

                    if (distToMain < bestDist) {
                        bestDist = distToMain;
                        bestBase = baseLocation;
                    }
                }

                return bestBase != null ? bestBase.position() : null;
            }
        );
    }
}
