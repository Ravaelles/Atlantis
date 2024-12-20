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
        if (true) return false;

        return unit.isRetreating()
            && unit.isMoving()
//            && unit.lastActionLessThanAgo(15, Actions.RUN_RETREAT)
            && !unit.lastStartedRunningMoreThanAgo(15)
//            && unit.combatEvalRelative() < 4.0
            && unit.distToTargetMoreThan(2)
            && unit.enemiesThatCanAttackMe(unit.woundPercent() >= 30 ? 4 : 2.5).notEmpty();
    }

    public Manager handle() {
        return usedManager(this);
    }
}
