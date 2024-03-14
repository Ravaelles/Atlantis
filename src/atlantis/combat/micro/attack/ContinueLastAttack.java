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
        if (continueRecentAttackNearbyEnemies()) return true;

        if (!unit.isAttacking()) return false;
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

    private boolean continueRecentAttackNearbyEnemies() {
        return unit.isActiveManager(AttackNearbyEnemies.class)
            && unit.lastActionLessThanAgo(1, Actions.ATTACK_UNIT);
    }

    @Override
    public Manager handle() {
        return usedManager(this);
    }
}
