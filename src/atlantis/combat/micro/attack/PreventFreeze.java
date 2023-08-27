package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Sometimes units can become "frozen" and not attack. This manager is responsible for preventing that.
 */
public class PreventFreeze extends Manager {
    public PreventFreeze(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.noCooldown()
            && unit.lastActionLessThanAgo(20, Actions.ATTACK_UNIT)
            && unit.hasNotMovedInAWhile()
            && unit.lastActionMoreThanAgo(30 * 5, Actions.HOLD_POSITION);
    }

    @Override
    public Manager handle() {
        unit.holdPosition("Unfreeze");
        return usedManager(this);
    }
}
