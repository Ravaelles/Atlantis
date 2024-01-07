package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class UnfreezeAttackOld extends Manager {
    public UnfreezeAttackOld(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return whenAttacking(unit);
    }

    private boolean whenAttacking(AUnit unit) {
        return
            unit.isMoving()
                && unit.noCooldown()
                && (unit.hasNotMovedInAWhile() || unit.lastActionLessThanAgo(62, Actions.ATTACK_UNIT))
                && (unit.hasTarget() && !unit.isTargetInWeaponRangeAccordingToGame(unit.target()));
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
