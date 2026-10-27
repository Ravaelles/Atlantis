package atlantis.map.base.define;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.ABaseLocation;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.util.cache.Cache;

public class BaseLocationsNearEnemy {
    private static final Cache<Positions<ABaseLocation>> cache = new Cache<>();

    public static Positions<ABaseLocation> get() {
        return cache.getIfValid(
            "get",
            30 * 11,
            () -> {
                HasPosition enemyCore = EnemyUnits.enemyMainBase();
                if (enemyCore == null) enemyCore = EnemyUnits.enemyBase();
                if (enemyCore == null) return new Positions<>();

                return BaseLocationsNearestTo.takeN(3, enemyCore);
            }
        );
    }
}
