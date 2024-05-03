package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ContinueShooting extends Manager {
    public ContinueShooting(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (We.terran()) return false;
        if (!unit.isAction(Actions.ATTACK_UNIT)) return false;

        if (unit.isStartingAttack()) return true;
        if (unit.isAttackFrame()) return true;

        if (unit.lastAttackFrameLessThanAgo(5)) return false;
        if (!unit.hasValidTarget()) return false;

        Decision decision;

        if (unit.isDragoon() && (decision = decisionForDragoon()).notIndifferent()) return decision.toBoolean();

//        if (unit.lastActionMoreThanAgo(8, Actions.ATTACK_UNIT)) return false;
//        if (unit.lastActionMoreThanAgo(10, Actions.ATTACK_UNIT)) return false;
        if (unit.isMelee() && unit.lastActionMoreThanAgo(50)) return false;

        if (unit.lastAttackFrameMoreThanAgo(30 * 6)) return true;

//        if (unit.isStopped()) return false;
//        if (ShouldRetreat.shouldRetreat(unit)) return false;
//
//        if (unit.isDragoon() && doesNotApplyForDragoon()) return false;
//        if (unit.isMarine() && doesNotApplyForMarine()) return false;

        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return true;

        return false;

//        if (true) return false;
//
////        if (
////            unit.isDragoon()
////                && unit.isMissionDefendOrSparta()
////                && unit.meleeEnemiesNearCount(1.2) >= 1
////        ) return false;
//
//        if (unit.lastActionMoreThanAgo(15)) return false;
//
////        if (unit.isMissionSparta() && unit.isDragoon() && unit.distToTarget() > 4) return false;
//
////        if (unit.isStartingAttack()) return true;
////        if (unit.isAttackFrame()) return true;
//
//        if (!unit.hasValidTarget()) return false;
////        if (unit.lastActionMoreThanAgo()) return false;
//
//        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return true;
//
////        System.out.println("@ " + A.now() + " - NOPE - ContinueShooting " + unit.id());
//        return unit.isTargetInWeaponRangeAccordingToGame(unit.target());
////        return unit.hasWeaponRangeByGame(unit.targetUnitToAttack());
    }

    private Decision decisionForDragoon() {
        if (unit.isAttacking() && unit.lastActionLessThanAgo(10, Actions.ATTACK_UNIT)) return Decision.ALLOWED;

        if (unit.lastAttackFrameLessThanAgo(1)) return Decision.FORBIDDEN;

        return (
            unit.lastActionLessThanAgo(50 + (unit.woundHp() <= 30 ? 50 : 0), Actions.ATTACK_UNIT)
                || (unit.hp() >= 21 && unit.lastAttackFrameMoreThanAgo(30 * 3))
        ) ? Decision.ALLOWED : Decision.INDIFFERENT;
    }

    private boolean doesNotApplyForDragoon() {
        double minDistToEnemy = 1.2 + unit.woundPercent() / 80.0;

        if (unit.hp() <= 30) return true;
        if (unit.meleeEnemiesNearCount(minDistToEnemy) >= 1) return true;

        return false;
    }

    private boolean doesNotApplyForMarine() {
        double minDistToEnemy = 1.5 + unit.woundPercent() / 80.0;

        if (unit.hp() <= 21) return true;
        if (!unit.hasMedicInRange() && unit.meleeEnemiesNearCount(minDistToEnemy) >= 1) return true;

        return false;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
