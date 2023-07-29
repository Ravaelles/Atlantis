package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DynamicRepairsNearby extends Manager {
    public DynamicRepairsNearby(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScv() && (unit.id() % 3 == 0 || unit.isRepairerOfAnyKind());
    }

    public Manager handle() {
        if (check()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean check() {
        if (!A.hasMinerals(15)) {
            return false;
        }

        AUnit repairable = unit.friendsNear().mechanical().wounded().inRadius(4, unit).nearestTo(unit);

        if (repairable != null && repairable.isWalkable()) {
            unit.repair(repairable, "KindGuy", false);

            if (repairable.looksIdle()) {
                repairable.move(unit, Actions.MOVE_REPAIR, "ToRepairer");
            }

            return true;
        }

        return false;
    }
}
