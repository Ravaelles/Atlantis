package atlantis.combat.missions.defend;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class PreventChasingEnemyWorkerInBase {
    public static boolean prevent(AUnit unit, AUnit enemy) {
        if (!enemy.isWorker()) return false;

        if (Enemy.zerg()
            && A.s <= 7 * 30
            && (unit.isMelee() || Count.dragoons() <= 2)
            && (
                !unit.isTargetInWeaponRangeAccordingToGame(enemy)
                    || unit.enemiesNear().combatUnits().atLeast(1)
                    || unit.distToFocusPoint() >= 12
        )
            && (unit.distToFocusPoint() >= 8 || enemy.distToMainChokeOr(-1) >= 12)
        ) return true;

        return false;

    }
}
