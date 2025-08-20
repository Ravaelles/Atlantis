package atlantis.combat.micro.dancing;

import atlantis.architecture.Manager;
import atlantis.combat.micro.dancing.away.protoss.dragoon.DanceAwayDragoon;
import atlantis.units.AUnit;

public class DanceAfterShoot extends Manager {
    public DanceAfterShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isRunningOrRetreating()) return false;

        return unit.attackState().finishedShooting();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DanceAwayIdle.class,
            DanceAwayDragoon.class,
//            DanceAwayAsMelee.class,
//            DanceAwayAsMarine.class,
//            DanceAwayAsTank.class,
//            DanceAway.class,
//            DanceTo.class,
//            DanceToAsDragoon.class,
//            DanceToAsMarine.class,
        };
    }

    private int cooldownRemainingThreshold() {
        return 7;
    }

    private boolean shouldSkip() {
        if (unit.isMelee()) return true;
        if (unit.target() == null) return true;

        if (unit.isMissionSparta()) return true;
//        if (!unit.isAttacking() && unit.noCooldown() && unit.woundPercent() <= 10) return true;

        // Terran
        if (unit.isMedic()) return true;
        if (unit.isWraith()) return true;
        if (unit.isTank()) return true;

        return false;
    }
}
