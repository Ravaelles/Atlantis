package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class PreventAttackTooLong extends Manager {
    public PreventAttackTooLong(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAttacking()) return false;
        if (unit.isMoving()) return false;
        if (unit.isRunning()) return false;
        if (unit.isStartingAttack()) return false;
        if (unit.isAttackFrame()) return false;

        int DELAY = 23;
        if (unit.lastActionLessThanAgo(DELAY)) return false;

        return unit.lastActionMoreThanAgo(unit.cooldownAbsolute() + DELAY, Actions.ATTACK_UNIT);
    }

    @Override
    public Manager handle() {
//        System.err.println("PreventAttackTooLong for " + unit.idWithType() + " / isAttacking:" + unit.isAttacking());

        return DoPreventLogic.handle(unit) ? usedManager(this) : null;
    }
}
