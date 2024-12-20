package atlantis.map.base.define;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.ABaseLocation;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.util.cache.Cache;

public class UnexploredBaseLocationNearEnemy {
    private static Cache<HasPosition> cache = new Cache<>();

    public static HasPosition get() {
        return cache.getIfValid(
            "get",
            30 * 11,
            () -> {
                HasPosition enemyCore = EnemyUnits.enemyMainBase();
                if (enemyCore == null) enemyCore = EnemyUnits.enemyBase();
                if (enemyCore == null) return null;

                return BaseLocationsNearestTo.takeN(3, enemyCore).unexplored().nearestTo(enemyCore.position());

//                for (ABaseLocation baseLocation : BaseLocationsUnexplored.list()) {
//                    double distToMain = enemyCore.groundDist(baseLocation);
//                    if (
//                        distToMain < bestDist
//                            && distToMain >= 12
//                            && distToNatural >= 12
//                    ) {
//                        bestDist = distToMain;
//                        bestBase = baseLocation;
//                    }
//                }
//
//                ABaseLocation baseLocation = DefineNaturalBase.naturalIfMainIsAt(enemyCore.position());
//                if (baseLocation != null) {
//                    return baseLocation.position().translateByTiles(2, 0);
//                }
//
//                return bestBase != null ? bestBase.position() : null;
            }
        );
    }
}
