package atlantis.information.enemy;

import atlantis.units.AUnit;

public class EnemyWhoBreachedBase {
    public static AUnit get() {
        return EnemyNearBases.enemyNearAnyOurBase(-1);
    }
}
