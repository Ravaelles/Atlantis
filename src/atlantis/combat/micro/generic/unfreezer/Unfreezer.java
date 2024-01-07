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
        return unit.isCombatUnit()
//            && unit.noCooldown()
            && !unit.isLoaded()
//            && !unit.isMoving()
//            && A.now() % 73 == 0
            && A.now() >= 20
//            && unit.looksIdle()
            && unit.lastPositionChangedMoreThanAgo(3);
//            && unit.lastActionMoreThanAgo(30 * 3, Actions.MOVE_UNFREEZE);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            UnfreezeAttack.class,
            UnfreezeRun.class,
//            UnfreezeRunA.class,
//            UnfreezeRunB.class,
        };
    }
}
