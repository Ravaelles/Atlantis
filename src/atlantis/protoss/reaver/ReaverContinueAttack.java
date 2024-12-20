package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class ReaverContinueAttack extends Manager {
    public ReaverContinueAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAttacking()) return false;

        if (unit.isStartingAttack()) return true;
        if (unit.isAttackFrame()) return true;

        if (
            unit.lastActionLessThanAgo(50, Actions.ATTACK_UNIT)
                && (unit.shieldHealthy() || unit.lastAttackFrameMoreThanAgo(70))
        ) return true;

//        if (unit.cooldownRemaining() >= 10) return false;

        return false;
    }

    @Override
    public Manager handle() {
        if (shouldContinueAttacking()) return usedManager(this, "GoOn");

        return null;
    }

    private boolean shouldContinueAttacking() {
        return unit.isAttacking()
            && unit.lastActionLessThanAgo(45)
            && unit.hasValidTarget();
//            && unit.isTargetInWeaponRangeAccordingToGame();
    }
}
