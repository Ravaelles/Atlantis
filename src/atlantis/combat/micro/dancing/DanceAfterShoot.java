package atlantis.combat.micro.dancing;

import atlantis.architecture.Manager;
import atlantis.combat.managers.*;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.UnitAttackWaitFrames;
import atlantis.units.special.SpecialUnitsManager;

public class DanceAfterShoot extends Manager {
    public DanceAfterShoot(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (!unit.isMarine()) return false;

        boolean applies = unit.isRanged()
//            && !unit.isAttacking()
//            && (!unit.hasTarget() || !unit.target().isABuilding())
            && !unit.isHoldingPosition()
            && !unit.isStartingAttack()
            && !unit.isAttackFrame()
//            && unit.cooldownRemaining() >= cooldownRemainingThreshold()
            && !shouldSkip()
//            && unit.cooldownRemaining() <= unit.cooldownAbsolute() - UnitAttackWaitFrames.effectiveStopFrames(unit.type())
//            && unit.lastActionMoreThanAgo(6, Actions.ATTACK_UNIT)
            && UnitAttackWaitFrames.waitedLongEnoughForAttackFrameToFinish(unit);
//            && UnitAttackWaitFrames.waitedLongEnoughForStartedAttack(unit)
//            && unit.enemiesNear().ranged().havingGreaterRanged().inRadius(6, unit).empty()

//        System.err.println("APPLIES = " + (applies));

        return applies;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            DanceAway.class,
            DanceTo.class,
        };
    }

    private int cooldownRemainingThreshold() {
        return 7;
    }

    private boolean shouldSkip() {
        if (unit.isMelee()) return true;
        if (unit.target() == null) return true;

        if (unit.isMissionSparta()) return true;
        if (!unit.isAttacking() && unit.noCooldown() && unit.woundPercent() <= 10) return true;

        // Terran
        if (unit.isMedic()) return true;
        if (unit.isWraith()) return true;
        if (unit.isTank()) return true;

        return false;
    }
}
