package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.fix.PreventDoNothing;

public class Unfreezer extends Manager {
    public Unfreezer(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (unit.isAir()) return false;
        if (!unit.isCombatUnit()) return false;
        if (unit.isABuilding()) return false;
        if (unit.hasCooldown()) return false;
//        if (unit.lastAttackFrameLessThanAgo(45)) return false;
        if (unit.lastPositionChangedLessThanAgo(29)) return false;
//        if (unit.isStartingAttack()) return false;
//        if (unit.isAttackFrame()) return false;
//        if (unit.hasCooldown()) return false;
//        if (unit.isMoving()) return false;
//        if (unit.lastActionLessThanAgo(55)) return false;
//        if (unit.lastPositionChangedLessThanAgo(unit.isMissionDefend() ? 30 * 5 : 45)) return false;
        if (A.s <= 1) return false;

//        if (unit.hasNotMovedInAWhile() && unit.lastActionMoreThanAgo(50)) return true;

        return true;

//        if (unit.isSpecialAction()) return false;
//        if (unit.lastPositionChangedMoreThanAgo(50) && unit.isStopped() && unit.noCooldown()) return true;
//
//        if (unit.lastActionLessThanAgo(10, Actions.MOVE_UNFREEZE) && !unit.isStopped()) return false;
//        if (unit.lastActionLessThanAgo(33)) return false;
//        if (unit.lastAttackFrameLessThanAgo(33)) return false;
////        if (unit.isHoldingPosition() && unit.isSpecialMission()) return false;
////        if (unit.isMissionDefendOrSparta() && unit.lastActionLessThanAgo(30 * 5)) return false;
//        if (unit.lastPositionChangedLessThanAgo(30)) return false;
//        if (unit.isActiveManager(PreventDoNothing.class)) return false;
//
//        return unit.isCombatUnit()
////            && unit.noCooldown()
//            && unit.isGroundUnit()
//            && !unit.isLoaded()
//            && !unit.isStartingAttack()
//            && !unit.isAttackFrame()
////            && !unit.isMoving()
////            && A.now() % 73 == 0
//            && A.now() >= 20
////            && unit.looksIdle()
//            && unit.lastActionMoreThanAgo(3, Actions.HOLD_POSITION);
////            && unit.lastActionMoreThanAgo(30 * 3, Actions.MOVE_UNFREEZE);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            UnfreezeDragoon.class,
            UnfreezeGeneric.class,
//            UnfreezeAttackOrMove.class,
//            UnfreezeRun.class,
//            UnfreezeRunA.class,
//            UnfreezeRunB.class,
        };
    }
}
