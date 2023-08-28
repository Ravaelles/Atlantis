package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.game.A;
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
            && unit.lastActionMoreThanAgo(30 * 2, Actions.HOLD_POSITION);
    }

    @Override
    public Manager handle() {
//        System.err.println("@ " + A.now() + " - UNFREEZE " + unit + " / " + unit.manager());
        unit.holdPosition("Unfreeze");
        return usedManager(this);
    }
}
