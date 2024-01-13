package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class UnfreezeAttackOrMove extends Manager {
    public UnfreezeAttackOrMove(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.lastActionMoreThanAgo(57, Actions.MOVE_UNFREEZE)) return false;

        return whenLongWithoutAction() || whenActionInit() || whenAttacking() || whenMoving();
    }

    private boolean whenLongWithoutAction() {
        return !unit.isWorker() && unit.lastActionMoreThanAgo(21);
    }

    private boolean whenActionInit() {
        return unit.action().equals(Actions.INIT);
    }

    private boolean whenMoving() {
        return (unit.isMoving() || unit.action().isMoving())
            && unit.lastActionMoreThanAgo(33)
            && unit.lastPositionChangedMoreThanAgo(5)
            && unit.targetPosition() != null;
    }

    private boolean whenAttacking() {
        return
            unit.isAttackingOrMovingToAttack()
//            && unit.noCooldown()
//            && (unit.hasNotMovedInAWhile() || unit.lastActionLessThanAgo(62, Actions.ATTACK_UNIT))
//                && unit.lastActionMoreThanAgo(3, Actions.ATTACK_UNIT)
                && unit.lastPositionChangedMoreThanAgo(4)
                && (unit.noTarget() || !unit.isTargetInWeaponRangeAccordingToGame(unit.target()));
    }

    @Override
    public Manager handle() {
//        System.err.println(A.now() + " Unfreezing ATTACK " + unit + " / " + unit.action());
//        if (unit.isMoving() || unit.) {
//            unit.holdPosition("Unfreeze");
//            return usedManager(this);
//        }

        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

        return null;
    }
}
