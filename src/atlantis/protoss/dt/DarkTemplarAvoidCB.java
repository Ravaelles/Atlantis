package atlantis.protoss.dt;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class DarkTemplarAvoidCB extends Manager {

    private Selection cbs;

    public DarkTemplarAvoidCB(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        cbs = unit.enemiesNear().combatBuildingsAntiLand().inRadius(12, unit);
        if (cbs.empty()) return false;

        if (
            unit.effUndetected()
                && unit.shields() >= 20
                && unit.lastUnderAttackMoreThanAgo(30 * 15)
        ) {
            AUnit cb = cbs.nearestTo(unit);
            if (noDetectorsNearCb(cb) && cb.enemiesNear().countInRadius(3, unit) >= 1) {
                return false;
            }
        }

        return true;
    }

    private boolean noDetectorsNearCb(AUnit cb) {
        return cb.friendsNear().detectors().inRadius(11, cb).empty();
    }

    @Override
    public Manager handle() {
        if (unit.moveToMain(Actions.MOVE_AVOID)) {
            return usedManager(this);
        }

        return null;
    }
}
