package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Protoss_Reaver;

public class ProtossShuttleEmptyGoToReaver extends Manager {
    private AUnit target;

    public ProtossShuttleEmptyGoToReaver(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.loadedUnits().isEmpty()) return false;

        target = definePotentialTargetToLift();
        if (target == null) return false;

        return unit.distTo(target) > 0.2;
    }

    private AUnit definePotentialTargetToLift() {
        Selection targets = unit.friendsNear().reavers().notLoaded();

        if (targets.empty()) {
            targets = Select.ourOfType(Protoss_Reaver).notLoaded();
        }

        if (targets.empty()) {
            targets = Select.ourOfType(Protoss_Reaver);
        }

        return targets.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if (unit.move(target, Actions.MOVE_FOLLOW, "BackupForReava")) return usedManager(this);

        return null;
    }
}
