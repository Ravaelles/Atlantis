package atlantis.combat.retreating;

import atlantis.architecture.Manager;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class ContinueRetreating extends Manager {
    public ContinueRetreating(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isRetreating()
            && unit.isMoving()
            && unit.lastActionLessThanAgo(15, Actions.RUN_RETREAT)
            && unit.distToTargetMoreThan(1.3);
    }

    public Manager handle() {
        return usedManager(this);
    }
}
