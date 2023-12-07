package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ContinueHeal extends Manager {
    public ContinueHeal(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !unit.isIdle() && unit.lastActionLessThanAgo(8, Actions.HEAL);
    }

    @Override
    public Manager handle() {
        unit.setTooltip("hEaL");
        return usedManager(this);
    }
}

