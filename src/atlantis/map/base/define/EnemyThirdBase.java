package atlantis.map.base.define;

import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class EnemyThirdBase {
    private static Cache<APosition> cache = new Cache<>();

    public static APosition get() {
        return cache.getIfValid(
            "get",
            30 * 23,
            () -> {
                HasPosition enemyBase = BaseLocations.enemyMain();
                if (enemyBase == null) return null;

                APosition enemyNatural = BaseLocations.enemyNatural();
                if (enemyNatural == null) {
                    ErrorLog.printErrorOnce(
                        "############## No enemy natural found, so no enemy third can be determined ##############"
                    );
                    return null;
                }

                double bestDist = 9999;
                ABaseLocation bestBase = null;

                for (ABaseLocation baseLocation : BaseLocations.baseLocations()) {
                    if (baseLocation.isStartLocation()) continue;

                    double distToMain = enemyBase.groundDist(baseLocation);
                    double distToNatural = enemyNatural.groundDist(baseLocation);
                    if (
                        distToMain < bestDist
                            && distToMain >= 26
                            && distToNatural >= 20
                    ) {
                        bestDist = distToMain;
                        bestBase = baseLocation;
                    }
                }

//                ABaseLocation baseLocation = DefineNaturalBase.naturalIfMainIsAt(enemyBase.position());
//                if (baseLocation != null) {
//                    return baseLocation.position().translateByTiles(2, 0);
//                }

                return bestBase != null ? bestBase.position() : null;
            }
        );
    }
}
