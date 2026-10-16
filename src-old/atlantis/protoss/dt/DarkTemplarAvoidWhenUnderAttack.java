package atlantis.protoss.dt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class DarkTemplarAvoidWhenUnderAttack extends Manager {
    public DarkTemplarAvoidWhenUnderAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.lastUnderAttackLessThanAgo(45);
    }

    @Override
    public Manager handle() {
        if (unit.moveToSafety(Actions.MOVE_AVOID)) {
            return usedManager(this, "DTUnderAttack");
        }

        return null;
    }
}
