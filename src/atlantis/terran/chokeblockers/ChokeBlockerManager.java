package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class ChokeBlockerManager extends Manager {
    private final AUnit otherBlocker;
    private final APosition goTo;

    public ChokeBlockerManager(AUnit unit, AUnit otherBlocker, APosition goTo) {
        super(unit);
        this.otherBlocker = otherBlocker;
        this.goTo = goTo;
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().notEmpty()
            || unit.friendsNear().nonBuildings().inRadius(8, unit).atMost(14);
    }

    @Override
    protected Manager handle() {
        if (shouldMoveAway()) return moveAway();

        if (otherBlocker == null || !otherBlocker.isAlive() || otherBlocker.isHealthy()) {
            unit.move(goTo, Actions.MOVE_SPECIAL, "ChokeBlocker");
        }
        else {
            unit.repair(otherBlocker, "RepairChoker");
        }

        return usedManager(this);
    }

    private boolean shouldMoveAway() {
        if (goTo.distTo(unit) > 8) return false;

//        return unit.enemiesNear().inRadius(5, unit).empty()
        return unit.enemiesNear().empty()
            && unit.friendsNear().workers().notProtector().inRadius(6, unit).size() >= 2;
    }

    private Manager moveAway() {
        unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_SPACE, "Spacing...");
        return usedManager(this);
    }
}
