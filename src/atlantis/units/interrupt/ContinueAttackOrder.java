package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

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
            && unit.canAttackTargetWithBonus(target, 1);
    }

    public Manager handle() {
        return usedManager(this);
    }
}
