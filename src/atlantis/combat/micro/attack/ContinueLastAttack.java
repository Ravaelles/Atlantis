package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;

import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ContinueLastAttack extends Manager {
    public ContinueLastAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;
//        if (unit.isMelee()) return false;

//        if (ShouldRetreat.shouldRetreat(unit)) return false;

        if (!unit.isAttacking()) return false;
        if (unit.lastActionMoreThanAgo(5)) return false;

        if (unit.isDragoon()) return asDragoon();
        
        if (continueRecentAttackNearbyEnemies()) return true;

//        if (!unit.isAttackingOrMovingToAttack()) return false;

        if (
            unit.hasValidTarget()
                && unit.canAttackTarget(unit.target())
                && (
                unit.lastActionLessThanAgo(4, Actions.ATTACK_UNIT)
//                    || unit.lastActionLessThanAgo(4, Actions.MOVE_ATTACK)
            )
        ) {
            return true;
        }

        return false;
    }

    private boolean asDragoon() {
        return unit.isDragoon()
            && unit.isAttacking()
            && unit.lastActionLessThanAgo(10, Actions.ATTACK_UNIT);
//            && unit.lastAttackFrameMoreThanAgo(20);
    }

    private boolean continueRecentAttackNearbyEnemies() {
        return unit.isActiveManager(AttackNearbyEnemies.class)
            && unit.lastActionLessThanAgo(1, Actions.ATTACK_UNIT);
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
