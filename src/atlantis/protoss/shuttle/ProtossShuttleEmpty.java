package atlantis.protoss.shuttle;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import static atlantis.units.AUnitType.Protoss_Reaver;

public class ProtossShuttleEmpty extends Manager {
    private AUnit target;

    public ProtossShuttleEmpty(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.loadedUnits().isEmpty()) return false;

        target = definePotentialTargetToLift();
        if (target == null) return false;

        return true;
    }

    private AUnit definePotentialTargetToLift() {
        Selection targets = unit.friendsNear().reavers().notLoaded().notDeadMan();

        if (targets.empty()) {
            targets = Select.ourOfType(Protoss_Reaver).notLoaded().notDeadMan();
        }

        return targets.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if (unit.move(target, Actions.MOVE_FOLLOW, "BackupForReava")) return usedManager(this);

        return null;
    }
}
