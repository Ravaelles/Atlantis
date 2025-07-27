package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class CorsairHuntMutas extends Manager {
    private AUnit muta;

    public CorsairHuntMutas(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!Enemy.zerg()) return false;

        return (muta = muta()) != null;
    }

    private AUnit muta() {
        Selection mutalisks = unit.enemiesNear().mutalisks().havingPosition();
        if (mutalisks.isEmpty()) return null;

        Selection mutalisksClose = mutalisks.inRadius(6, unit);
        if (mutalisksClose.notEmpty()) {
            return mutalisksClose.mostWoundedOrNearest(unit);
        }

        return mutalisks.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if (unit.attackUnit(muta)) {
            return usedManager(this, "CorsairHunt");
        }

        return null;
    }
}
