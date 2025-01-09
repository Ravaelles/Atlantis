package atlantis.protoss.corsair;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class CorsairHuntKnownOverlords extends Manager {
    private AUnit overlord;

    public CorsairHuntKnownOverlords(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!Enemy.zerg()) return false;

        return (overlord = overlord()) != null;
    }

    private AUnit overlord() {
        Selection overlords = Select.enemy().ofType(AUnitType.Zerg_Overlord).sortByNearestTo(Select.mainOrAnyBuilding());
        if (overlords.isEmpty()) return null;

        for (AUnit overlord : overlords.list()) {
            if (overlord.friendsNear().havingAntiAirWeapon().countInRadius(7.1, overlord) == 0) {
                return overlord;
            }
        }

        return null;
    }

    @Override
    public Manager handle() {
        if (unit.attackUnit(overlord)) {
            return usedManager(this, "HuntKnownOverlords");
        }

        return null;
    }
}
