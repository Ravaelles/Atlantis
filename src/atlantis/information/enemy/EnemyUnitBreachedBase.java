package atlantis.information.enemy;

import atlantis.units.AUnit;

public class EnemyUnitBreachedBase {

    public static AUnit get() {
        return EnemyNearBases.enemyNearAnyOurBase(-1);
    }

    public static boolean someone() {
        return get() != null;
    }

    public static boolean noone() {
        return get() == null;
    }
}
