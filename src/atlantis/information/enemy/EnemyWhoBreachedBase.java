package atlantis.information.enemy;

import atlantis.combat.missions.Missions;
import atlantis.units.AUnit;

public class EnemyWhoBreachedBase {
    private static AUnit _lastEnemy = null;
    private static int _numOfBaseAttacks = 0;

    public static AUnit get() {
        AUnit enemy = EnemyNearBases.enemyNearAnyOurBase(-1);

        if (enemy != null && _lastEnemy == null && Missions.isGlobalMissionAttack()) {
            _numOfBaseAttacks++;
        }

        _lastEnemy = enemy;
        return enemy;
    }

    public static int numberOfAttacksOnBase() {
        return _numOfBaseAttacks;
    }

    public static boolean notNull() {
        return get() != null;
    }
}
