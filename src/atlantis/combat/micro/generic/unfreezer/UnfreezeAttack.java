package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class UnfreezeAttack extends Manager {
    public UnfreezeAttack(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return whenAttacking(unit);
    }

    private boolean whenAttacking(AUnit unit) {
        return unit.lastActionLessThanAgo(40, Actions.ATTACK_UNIT)
            && unit.noCooldown()
            && !unit.isMoving()
            && (unit.hasTarget() && !unit.isTargetInWeaponRangeAccordingToGame(unit.target()));
    }

    @Override
    public Manager handle() {
//        System.err.println(A.now() + " Unfreezing " + unit + " / " + unit.action());
        if (unit.isMoving()) {
            unit.holdPosition("Unfreeze");
            return usedManager(this);
        }

        if (unit.distToFocusPoint() >= 3) {
            unit.moveTactical(unit.micro().focusPoint(), Actions.MOVE_UNFREEZE, "Unfreeze");
        }
        else {
            AUnit goTo = Select.ourBuildings().random();
            if (goTo == null) goTo = unit.friendsNear().mostDistantTo(unit);
            if (goTo == null) return null;

            unit.moveTactical(goTo, Actions.MOVE_UNFREEZE, "Unfreezing");
        }

        return usedManager(this);
    }
}