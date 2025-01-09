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
        if (!A.hasMinerals(15)) {
            removeRepairer();
            return false;
        }

        AUnit repairable = repairable();

        if (repairable != null && repairable.isWalkable() && repairable.isAlive()) {
            if (ShouldNotRepairUnit.shouldNotRepairUnit(unit, repairable)) return false;

            if (RepairAssignments.countRepairersForUnit(repairable) >= 4) return false;

            RepairAssignments.addRepairer(unit, repairable);
            if (!unit.isRepairing()) {
                unit.repair(repairable, "DynaRepair");
                return true;
            }

            if (repairable.isABuilding() || repairable.looksIdle()) {
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

//        if (!unit.isRepairing() && !unit.hasNotMovedInAWhile()) {
        if (!unit.isRepairing() && !unit.hasNotMovedInAWhile()) {
            if (!canRemoveRepairer(repairable)) {
                removeRepairer();
                return false;
            }
        }

        return false;
    }

    private void removeRepairer() {
        RepairAssignments.removeRepairer(unit);
        RepairAssignments.removeProtector(unit);
    }

    private boolean canRemoveRepairer(AUnit target) {
        if (target == null || target.hp() <= 0) return false;
        if (unit.isProtector()) return false;

        return target.enemiesNear().havingWeapon().countInRadius(7, target) >= 2;
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
