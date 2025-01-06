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
        if (!We.protoss()) return false;

//        if (unit.isMarine()) {
//            return false;
////            if (unit.hp() <= 18) return false;
////            if (unit.hp() <= 32 && !unit.hasMedicInRange()) return false;
//        }

        if (!unit.isAttacking()) return false;
        if (!unit.isAction(Actions.ATTACK_UNIT)) return false;

        AUnit target = unit.target();

        return target != null
            && target.hasPosition()
            && target.isAlive()
            &&
            (
                unit.lastActionLessThanAgo(8)
                    || unit.lastActionLessThanAgo(unit.attackWaitFrames(), Actions.ATTACK_UNIT)
            )
            && unit.canAttackTargetWithBonus(target, 0.2);
//            && ContinueShootingAsDragoon.asDragoon(unit).notForbidden();
    }

    public Manager handle() {
//        System.out.println("ContinueVeryRecentAttack");
        return usedManager(this);
    }
}
