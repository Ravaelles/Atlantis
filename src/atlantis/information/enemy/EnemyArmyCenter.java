package atlantis.information.enemy;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.util.cache.Cache;

public class EnemyArmyCenter {
    private static Cache<APosition> cache = new Cache<>();

    public static HasPosition get() {
        return cache.get(
            "get",
            43,
            () -> EnemyUnits.discovered().combatUnits().center()
        );
    }
}
