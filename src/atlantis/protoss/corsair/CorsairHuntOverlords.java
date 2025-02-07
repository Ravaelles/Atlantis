package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class CorsairHuntOverlords extends Manager {
    private AUnit overlord;

    public CorsairHuntOverlords(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!Enemy.zerg()) return false;

        return (overlord = overlord()) != null;
    }

    private AUnit overlord() {
        Selection overlords = unit.enemiesNear().overlords();
        if (overlords.isEmpty()) return null;

        Selection overlordsClose = overlords.inRadius(6, unit);
        if (overlordsClose.notEmpty()) {
            return overlordsClose.mostWoundedOrNearest(unit);
        }

        return overlords.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if (unit.attackUnit(overlord)) {
            return usedManager(this, "CorsairHunt");
        }

        return null;
    }
}
