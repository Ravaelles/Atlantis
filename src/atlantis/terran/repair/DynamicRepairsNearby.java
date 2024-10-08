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
        return unit.isScv()
            && (unit.id() % 4 == 0 && !unit.isRepairerOfAnyKind())
            && !unit.isRepairing();
    }

    protected Manager handle() {
        if (check()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean check() {
        if (!A.hasMinerals(15)) return false;

        AUnit repairable = repairable();

        if (repairable != null && repairable.isWalkable() && repairable.isAlive()) {
            if (ShouldNotRepairUnit.shouldNotRepairUnit(unit, repairable)) return false;

            if (RepairAssignments.countRepairersForUnit(repairable) >= 4) return false;

            RepairAssignments.addRepairer(unit, repairable);
            if (!unit.isRepairing()) {
                unit.repair(repairable, "DynaRepair");
            }

            if (repairable.looksIdle()) {
                if (repairable.isScv()) {
                    repairable.repair(unit, "LoveBack");
                    return true;
                }

//                if (repairable.distTo(unit) > 0.3) {
//                    unit.move(unit, Actions.MOVE_REPAIR, "ToRepairer");
//                    return true;
//                }
            }
        }

        if (!unit.isRepairing() && !unit.hasNotMovedInAWhile()) {
            RepairAssignments.removeRepairer(unit);
            return false;
        }

        return false;
    }

    private AUnit repairable() {
        return unit.friendsNear()
            .mechanical()
            .wounded()
            .exclude(unit)
            .inRadius(2.5, unit)
            .hasPathFrom(unit)
            .nearestTo(unit);
    }
}
