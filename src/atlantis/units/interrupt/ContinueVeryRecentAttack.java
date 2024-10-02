package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ContinueVeryRecentAttack extends Manager {
    public ContinueVeryRecentAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (We.terran()) return false;

//        if (unit.isMarine()) {
//            return false;
////            if (unit.hp() <= 18) return false;
////            if (unit.hp() <= 32 && !unit.hasMedicInRange()) return false;
//        }

        if (!unit.isAttacking()) return false;

        AUnit target = unit.target();

        return target != null
            && target.hasPosition()
            && target.isAlive()
            && unit.lastActionLessThanAgo(5)
            && unit.lastActionLessThanAgo(unit.attackWaitFrames(), Actions.ATTACK_UNIT)
            && unit.canAttackTargetWithBonus(target, 1);
//            && ContinueShootingAsDragoon.asDragoon(unit).notForbidden();
    }

    public Manager handle() {
        return usedManager(this);
    }
}
