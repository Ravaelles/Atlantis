package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.terran.chokeblockers.ChokeBlockersAssignments;
import atlantis.terran.chokeblockers.ChokeToBlock;
import atlantis.units.AUnit;

public class UnfreezeGeneric extends Manager {
    public UnfreezeGeneric(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.hasCooldown()) return false;
        if (unit.isDragoon()) return false;
        if (unit.isReaver()) return false;
        if (unit.hasCooldown()) return false;
        if (unit.lastPositionChangedLessThanAgo(72)) return false;
//        if (unit.lastActionLessThanAgo(52)) return false;

//        if (true) return false;

        if (ChokeBlockersAssignments.get().isChokeBlocker(unit)) return false;
//
//        if (unit.hasCooldown()) return false;
//        if (unit.isAccelerating()) return false;
//        if (duringMissionAttack()) return false;
//        if (unit.lastPositionChangedLessThanAgo(42)) return false;
//        if (unit.lastActionLessThanAgo(16)) return false;
//        if (unit.isActiveManager(UnfreezeGeneric.class) && unit.lastActionLessThanAgo(3)) return false;
////        if (unit.isAttacking()) return false;
//        if (unit.lastStartedAttackLessThanAgo(20)) return false;
//        if (unit.lastActionLessThanAgo(20, Actions.MOVE_DANCE_AWAY)) return false;

//        if (isDragoonDuringSpartaMission()) return false;
//        else if (isDuringSpartaMission()) return false;
//        else if (isDuringMissionAttack()) return false;

        return true;
    }

//    private boolean isDuringMissionAttack() {
//        return unit.isMissionAttack()
//            && unit.noCooldown()
//            && unit.lastPositionChangedMoreThanAgo(52);
//    }
//
//    private boolean isDuringSpartaMission() {
//        return unit.isMissionSparta()
//            && unit.distToOr999(ChokeToBlock.get()) <= 3;
//    }
//
//    private boolean isDragoonDuringSpartaMission() {
//        return unit.isDragoon()
//            && unit.isMissionSparta()
//            && unit.noCooldown()
//            && unit.lastPositionChangedMoreThanAgo(50)
//            && unit.distToOr999(ChokeToBlock.get()) <= 3
//            && unit.enemiesNear().zealots().inRadius(1, unit).notEmpty()
//            && (
//            unit.lastAttackFrameLessThanAgo(30)
//                || unit.friendsNear().zealots().inRadius(5, unit).notEmpty()
//        );
//    }

    @Override
    public Manager handle() {
        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

        return null;
    }
}
