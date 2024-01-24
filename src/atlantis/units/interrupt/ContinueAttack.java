package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.protoss.ContinueDragoonAttackOrder;

public class ContinueAttack extends Manager {
    public ContinueAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMarine()) {
            return false;
//            if (unit.hp() <= 18) return false;
//            if (unit.hp() <= 32 && !unit.hasMedicInRange()) return false;
        }

        if (!unit.isAttacking()) return false;

        AUnit target = unit.target();

        return target != null
            && target.hasPosition()
            && target.isAlive()
            && unit.lastActionLessThanAgo(unit.attackWaitFrames(), Actions.ATTACK_UNIT)
            && unit.canAttackTargetWithBonus(target, 1)
            && ContinueDragoonAttackOrder.asDragoon(unit).notForbidden();
    }

    public Manager handle() {
        return usedManager(this);
    }
}
