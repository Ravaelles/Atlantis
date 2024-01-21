package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class Unfreezer extends Manager {
    public Unfreezer(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (unit.isSpecialMission()) return false;
        if (unit.lastActionLessThanAgo(50)) return false;
        if (unit.lastPositionChangedLessThanAgo(20)) return false;

        return unit.isCombatUnit()
//            && unit.noCooldown()
            && !unit.isAir()
            && !unit.isLoaded()
//            && !unit.isMoving()
//            && A.now() % 73 == 0
            && A.now() >= 20
//            && unit.looksIdle()
            && unit.lastPositionChangedMoreThanAgo(12)
            && unit.lastActionMoreThanAgo(2, Actions.HOLD_POSITION);
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
