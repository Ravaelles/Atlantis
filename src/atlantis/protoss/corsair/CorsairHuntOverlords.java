package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class CorsairHuntOverlords extends Manager {
    private AUnit overlord;

    public CorsairHuntOverlords(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!Enemy.zerg()) return false;
        if (unit.lastUnderAttackLessThanAgo(30 * 3)) return false;

        return (overlord = overlord()) != null;
    }

    private AUnit overlord() {
        Selection overlords = unit.enemiesNear().overlords();
        if (overlords.isEmpty()) return null;

        Selection overlordsClose = overlords.inRadius(6, unit);
        if (overlordsClose.notEmpty()) {
            return overlordsClose.mostWoundedOrNearest(unit);
        }

        return EnemyUnits.discovered().overlords().nearestTo(unit);
//        return overlords.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if (unit.cooldown() >= 10 && unit.distTo(overlord) >= 5) {
            unit.move(overlord, Actions.MOVE_DANCE_TO);
            return usedManager(this, "CorsairCloserHunt");
        }

        if (unit.attackUnit(overlord)) {
            return usedManager(this, "CorsairHunt");
        }

        return null;
    }
}
