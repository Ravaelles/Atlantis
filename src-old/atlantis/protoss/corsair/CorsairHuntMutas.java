package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class CorsairHuntMutas extends Manager {
    private AUnit muta;

    public CorsairHuntMutas(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!Enemy.zerg()) return false;
        if (unit.hp() <= 42) return false;

        return (muta = muta()) != null;
    }

    private AUnit muta() {
        Selection mutalisks = Select.enemyCombatUnits().mutalisks().havingPosition();
        if (mutalisks.isEmpty()) return null;

        Selection mutalisksClose = mutalisks.inRadius(6, unit);
        if (mutalisksClose.notEmpty()) {
            return mutalisksClose.mostWoundedOrNearest(unit);
        }

        AUnit mutaNearBase = mutalisks.nearestTo(Select.mainOrAnyBuilding());
        if (mutaNearBase != null) return mutaNearBase;

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
