package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.decions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.protoss.ContinueDragoonAttack;

public class ContinueAttackOrder extends Manager {
    public ContinueAttackOrder(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMarine()) {
            if (unit.hp() <= 18) return false;
            if (unit.hp() <= 32 && !unit.hasMedicInRange()) return false;
        }

        AUnit target = unit.target();

        return unit.lastActionLessThanAgo(unit.attackWaitFrames(), Actions.ATTACK_UNIT)
            && target != null
            && target.hasPosition()
            && target.isAlive()
            && unit.canAttackTargetWithBonus(target, 1)
            && asDragoon();
    }

    private boolean asDragoon() {
        Decision decision;

        if ((decision = ContinueDragoonAttack.asDragoon(unit)).notIndifferent()) {
//            System.out.println("@ " + A.now() + " - DECISION ContinueAttackOrder " + decision);
            return decision.toBoolean();
        }

        return true;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
