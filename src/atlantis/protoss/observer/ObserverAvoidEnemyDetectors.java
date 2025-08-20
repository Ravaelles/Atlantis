package atlantis.protoss.observer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ObserverAvoidEnemyDetectors extends Manager {
    private Selection detectors;

    public ObserverAvoidEnemyDetectors(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isObserver()
            && (detectors = unit.enemiesNear().detectors()).notEmpty()
            && detectors.countInRadius(10, unit) >= 1;
    }

    @Override
    public Manager handle() {
        AUnit friendNear = friendNear();

        if (friendNear != null && unit.distTo(friendNear) > 2 && unit.move(friendNear, Actions.MOVE_SAFETY)) {
            return usedManager(this);
        }

        if (unit.moveAwayFrom(detectors.nearestTo(unit), 4, Actions.MOVE_SAFETY, "ObsAvoidDetectors")) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit friendNear() {
        return unit.friendsNear().combatUnits().groundUnits().inRadius(6, unit).nearestTo(unit);
    }
}
