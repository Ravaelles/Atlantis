package atlantis.information.enemy;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;

public class EnemyCloserToBaseThanAlpha {
    private static AUnit _lastEnemy = null;


    public static AUnit get() {
        AUnit enemy = EnemyNearBases.enemyNearAnyOurBase(-1);

        _lastEnemy = enemy;
        return enemy;
    }

    public static boolean notNull() {
        return get() != null;
    }

    public static boolean noone() {
        return get() == null;
    }
}
