package atlantis.combat.missions.defend;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class PreventChasingEnemyWorkerInBase {
    public static boolean prevent(AUnit unit, AUnit enemy) {
        if (enemy.isWorker()) return false;

        if (Enemy.zerg()
            && A.s <= 6 * 30
            && !unit.isTargetInWeaponRangeAccordingToGame(enemy)
            && (unit.distToFocusPoint() >= 10 || unit.eval() <= 1.5)
            && (unit.isMelee() || Count.dragoons() <= 3)
        ) return true;

        return false;

    }
}
