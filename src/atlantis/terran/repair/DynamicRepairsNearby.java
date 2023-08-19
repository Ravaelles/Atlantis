package atlantis.terran.repair;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class DynamicRepairsNearby extends Manager {
    public DynamicRepairsNearby(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isScv() && (unit.id() % 3 == 0 || unit.isRepairerOfAnyKind()) && !unit.isRepairing();
    }

    protected Manager handle() {
        if (check()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean check() {
        if (!A.hasMinerals(15)) return false;

        AUnit repairable = unit.friendsNear().mechanical().wounded().inRadius(4, unit).nearestTo(unit);

        if (repairable != null && repairable.isWalkable()) {
            if (ShouldNotRepairUnit.shouldNotRepairUnit(repairable)) return false;

            unit.repair(repairable, "KindGuy");

            if (repairable.looksIdle()) {
                repairable.repair(unit, "ToRepairer");
            }

            return true;
        }

        return false;
    }
}
