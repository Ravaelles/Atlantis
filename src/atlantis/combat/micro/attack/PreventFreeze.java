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
        return unit.noCooldown() && looksFrozen();
    }

    private boolean looksFrozen() {
        if (unit.lastActionMoreThanAgo(35, Actions.HOLD_POSITION)) {
            if (unit.lastActionLessThanAgo(20, Actions.ATTACK_UNIT) && unit.hasNotMovedInAWhile()) return true;
//            if (unit.looksIdle()) {
//                System.err.println("@ " + A.now() + " - looks idle unfreeze " + unit + " / " + unit.manager());
//            }
            if (unit.looksIdle()) return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
//        System.err.println("@ " + A.now() + " - UNFREEZE " + unit + " / " + unit.manager());
        unit.holdPosition("Unfreeze");
        return usedManager(this);
    }
}
