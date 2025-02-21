package atlantis.combat.micro.dancing.hold;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ContinueHoldToShoot extends Manager {
    private AUnit target;

    public ContinueHoldToShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        return unit.isHoldingPosition()
//        return unit.isAction(Actions.HOLD_TO_SHOOT)
        return
//            print("isActionHOLD_TO_SHOOT = " + unit.isAction(Actions.HOLD_TO_SHOOT))
            unit.isAction(Actions.HOLD_TO_SHOOT)
//            && print("Target = " + unit.target())
                && (target = unit.target()) != null
//            && print("NotInRange = " + !unit.isTargetInWeaponRangeAccordingToGame(target))
                && !unit.isTargetInWeaponRangeAccordingToGame(target)
//            && print("lastActionLessThanAgo = " + unit.lastActionLessThanAgo(30, Actions.HOLD_TO_SHOOT))
                && unit.lastActionLessThanAgo(30, Actions.HOLD_TO_SHOOT);
    }

    @Override
    public Manager handle() {
        unit.holdPosition(Actions.HOLD_TO_SHOOT, "HoldToShoot");
//        System.err.println("ContinueHoldToShoot / dist:" + unit.distToTargetDigit());
        return usedManager(this);
    }
}
