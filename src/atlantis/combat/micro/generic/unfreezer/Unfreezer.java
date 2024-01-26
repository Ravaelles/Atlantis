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
        if (unit.hasCooldown()) return false;
        if (unit.lastActionLessThanAgo(40)) return false;
//        if (unit.isHoldingPosition() && unit.isSpecialMission()) return false;
        if (unit.isSpecialMission()) return false;
        if (unit.lastPositionChangedLessThanAgo(30)) return false;
        if (unit.isActiveManager(PreventDoNothing.class)) return false;

        return unit.isCombatUnit()
//            && unit.noCooldown()
            && unit.isGroundUnit()
            && !unit.isLoaded()
            && !unit.isStartingAttack()
            && !unit.isAttackFrame()
//            && !unit.isMoving()
//            && A.now() % 73 == 0
            && A.now() >= 20
//            && unit.looksIdle()
            && unit.lastActionMoreThanAgo(3, Actions.HOLD_POSITION);
//            && unit.lastActionMoreThanAgo(30 * 3, Actions.MOVE_UNFREEZE);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            UnfreezeGeneric.class,
//            UnfreezeAttackOrMove.class,
            UnfreezeRun.class,
//            UnfreezeRunA.class,
//            UnfreezeRunB.class,
        };
    }
}
