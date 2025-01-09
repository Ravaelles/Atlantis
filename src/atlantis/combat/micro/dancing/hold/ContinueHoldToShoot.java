package atlantis.combat.micro.dancing.hold;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ContinueHoldToShoot extends Manager {
    private AUnit target;

    public ContinueHoldToShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

//        return unit.isHoldingPosition()
//        return unit.isAction(Actions.HOLD_TO_SHOOT)
        return
//            print("isActionHOLD_TO_SHOOT = " + unit.isAction(Actions.HOLD_TO_SHOOT))
            unit.isAction(Actions.HOLD_TO_SHOOT)
//            && print("Target = " + unit.target())
                && hasTarget()
//            && print("NotInRange = " + !unit.isTargetInWeaponRangeAccordingToGame(target))
//                && !unit.isTargetInWeaponRangeAccordingToGame(target)
//            && print("lastActionLessThanAgo = " + unit.lastActionLessThanAgo(30, Actions.HOLD_TO_SHOOT))
                && unit.lastActionLessThanAgo(30, Actions.HOLD_TO_SHOOT);
    }

    private boolean hasTarget() {
        if ((target = unit.target()) != null && !unit.isOtherUnitShowingBackToUs(target)) return true;

//        if (unit.enemiesICanAttack(3).count() > 0) return true;

//        System.err.println(A.now() + " ContinueHoldToShoot / No target");
        return false;
    }

    @Override
    public Manager handle() {
        if (unit.isMelee() && !unit.isTargetInWeaponRangeAccordingToGame(target)) {
            unit.holdPosition(Actions.HOLD_TO_SHOOT, "HoldToShoot");
        }

//        System.err.println("ContinueHoldToShoot / dist:" + unit.distToTargetDigit());
        return usedManager(this);
    }
}
