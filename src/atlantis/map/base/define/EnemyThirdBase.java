package atlantis.map.base.define;

import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

public class EnemyThirdBase {
    private static Cache<APosition> cache = new Cache<>();

    public static APosition position() {
        return cache.getIfValid(
            "get",
            30 * 23,
            () -> {
                HasPosition enemyMain = BaseLocations.enemyMain();
                if (enemyMain == null) return null;

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

                    double distToMain = enemyMain.groundDist(baseLocation);

                    if (distToMain <= 40) continue;

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

//                ABaseLocation baseLocation = DefineNaturalBase.naturalIfMainIsAt(enemyMain.position());
//                if (baseLocation != null) {
//                    return baseLocation.position().translateByTiles(2, 0);
//                }

                return bestBase != null ? bestBase.position() : null;
            }
        );
    }

    public static AUnit get() {
        APosition position = position();
        if (position == null) return null;

        return EnemyUnits.discovered().bases().inRadius(7, position).first();
    }
}
