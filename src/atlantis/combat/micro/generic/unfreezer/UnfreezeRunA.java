package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class UnfreezeRunA extends Manager {

    private boolean simpleRunFix;

    public UnfreezeRunA(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        System.err.println(
//            unit.lastActionAgo(Actions.MOVE_UNFREEZE)
//                + " / " +
//                unit.action()
//                + " / " +
//                unit.lastActionFramesAgo()
//        );

        return isSimpleRunFix();
    }

    private boolean isSimpleRunFix() {
        return unit.lastStartedRunningMoreThanAgo(20)
            && !unit.lastStartedRunningLessThanAgo(7)
            && unit.hasNotMovedInAWhile();
    }

    @Override
    public Manager handle() {
        if (simpleRunFix) {
//            System.out.println("@ " + A.now() + " - UNFREEZE RUN A " + unit);
//            if (A.now() % 2 == 0) {
//                unit.holdPosition("UnfreezeA");
//            }
//            else {
//                unit.stop("UnfreezeB");
//            }

            UnfreezerShakeUnit.shake(unit);

            return usedManager(this);
        }

        return null;
    }
}
