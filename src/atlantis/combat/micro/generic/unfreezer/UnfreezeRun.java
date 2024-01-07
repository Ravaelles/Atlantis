package atlantis.combat.micro.generic.unfreezer;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class UnfreezeRun extends Manager {

    private boolean simpleRunFix;

    public UnfreezeRun(AUnit unit) {
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

        return unit.isRunning()
            && unit.lastStartedRunningMoreThanAgo(5)
            && unit.lastPositionChangedMoreThanAgo(5);
    }

    @Override
    public Manager handle() {
//        System.err.println();
//        System.err.println(unit.typeWithUnitId());
//        System.err.println("unit.lastPositionChangedAgo() = " + unit.lastPositionChangedAgo());
//        System.err.println("unit.LAST RUN() = " + unit.lastStartedRunningAgo());

        if (UnfreezerShakeUnit.shake(unit)) return usedManager(this);

        return null;
    }
}
