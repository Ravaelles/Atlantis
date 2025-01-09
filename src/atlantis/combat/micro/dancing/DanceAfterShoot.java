package atlantis.combat.micro.dancing;

import atlantis.architecture.Manager;
import atlantis.combat.micro.dancing.away.DanceAway;
import atlantis.combat.micro.dancing.away.DanceAwayAsMarine;
import atlantis.combat.micro.dancing.away.DanceAwayAsMelee;
import atlantis.combat.micro.dancing.away.protoss.DanceAwayAsDragoon;
import atlantis.combat.micro.dancing.away.protoss.DanceAwayAsTank;
import atlantis.combat.micro.dancing.to.DanceTo;
import atlantis.combat.micro.dancing.to.DanceToAsDragoon;
import atlantis.combat.micro.dancing.to.DanceToAsMarine;
import atlantis.units.AUnit;

public class DanceAfterShoot extends Manager {
    public DanceAfterShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (unit.isMarine()) return false;

//        if (true) return false;

        boolean applies = unit.isRanged()
            && unit.attackState().finishedShooting();

//        boolean applies = unit.isRanged()
////            && !unit.isAttacking()
////            && (!unit.hasTarget() || !unit.target().isABuilding())
//            && !unit.isHoldingPosition()
//            && !unit.isStartingAttack()
//            && !unit.isAttackFrame()
//            && unit.lastActionMoreThanAgo(6, Actions.ATTACK_UNIT)
//            && unit.lastAttackFrameLessThanAgo(unit.cooldownAbsolute())
////            && applyAsDragoon()
////            && unit.cooldownRemaining() >= cooldownRemainingThreshold()
//            && !shouldSkip();

//        System.err.println("APPLIES = " + (applies));

        return applies;
    }

//    private boolean applyAsDragoon() {
//        if (!unit.isDragoon()) return true;
//
////        if (unit.cooldown() >= 24) return false;
////        if (unit.cooldown() <= 3) return false;
//
//        return true;
//    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DanceAway.class,
            DanceAwayAsDragoon.class,
            DanceAwayAsMelee.class,
            DanceAwayAsMarine.class,
            DanceAwayAsTank.class,
            DanceTo.class,
            DanceToAsDragoon.class,
            DanceToAsMarine.class,
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
