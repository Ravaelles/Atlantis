package atlantis.combat.missions.defend.protoss;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class PreventChasingEnemyLingInBase {
    public static boolean prevent(AUnit unit, AUnit enemy) {
        if (!Enemy.zerg()) return false;
        if (!enemy.isZergling()) return false;

        if (A.s <= 8 * 30
            && !unit.isTargetInWeaponRangeAccordingToGame(enemy)
            && unit.distTo(enemy) >= 4
            && enemy.groundDistToMain() <= 30
            && enemy.friendsNear().zerglings().countInRadius(3, unit) == 0
            && (unit.distToFocusPoint() >= 10 || unit.eval() <= 1.5)
        ) return true;

        return false;

    }
}
