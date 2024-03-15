package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class PreventAttackForTooLong extends Manager {
    public PreventAttackForTooLong(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        if (!unit.isAttacking()) return false;
        if (unit.isMoving()) return false;
        if (unit.isRunning()) return false;
        if (unit.isStartingAttack()) return false;
        if (unit.isAttackFrame()) return false;

        int DELAY = 113;
        if (unit.lastActionLessThanAgo(DELAY) && unit.lastAttackFrameMoreThanAgo(DELAY - 3)) {
            return false;
        }

        if (unit.isMissionSparta() && unit.isMelee()) return false;

        return unit.lastActionMoreThanAgo(unit.cooldownAbsolute() + DELAY, Actions.ATTACK_UNIT);
    }

    @Override
    public Manager handle() {
//        System.err.println("PreventAttackForTooLong for " + unit.idWithType() + " / isAttacking:" + unit.isAttacking());

        return DoPreventFreezesLogic.handle(unit) ? usedManager(this) : null;
    }
}
