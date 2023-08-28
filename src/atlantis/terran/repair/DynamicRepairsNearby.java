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
        return unit.isScv() && (unit.id() % 3 == 0 && !unit.isRepairerOfAnyKind()) && !unit.isRepairing();
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
            if (ShouldNotRepairUnit.shouldNotRepairUnit(unit, repairable)) return false;

            RepairAssignments.addRepairer(unit, repairable);
            if (!unit.isRepairing()) {
                unit.repair(unit, "DynaRepair");
            }

            unit.setTooltip("KindGuy");

            if (repairable.looksIdle()) {
                if (repairable.isScv()) {
                    repairable.repair(unit, "LoveBack");
                    return true;
                }

                if (repairable.distTo(unit) > 0.3) {
                    repairable.move(unit, Actions.MOVE_REPAIR, "ToRepairer");
                }
            }

            return true;
        }

        return false;
    }
}
