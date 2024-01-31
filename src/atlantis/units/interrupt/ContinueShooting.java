package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;

public class ContinueShooting extends Manager {
    public ContinueShooting(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAttacking()) return false;
        if (unit.hp() <= 20 && unit.isDragoon()) return false;
        if (unit.lastActionMoreThanAgo(25)) return false;

//        if (unit.isMissionSparta() && unit.isDragoon() && unit.distToTarget() > 4) return false;
        if (unit.isDragoon() && unit.isMissionDefendOrSparta()) return false;

//        if (!unit.isStartingAttack()) return false;
//        if (!unit.isAttackFrame()) return false;

        if (unit.isStartingAttack()) return true;
        if (unit.isAttackFrame()) return true;

        if (!unit.hasValidTarget()) return false;
//        if (unit.lastActionMoreThanAgo()) return false;

        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return true;

//        System.out.println("@ " + A.now() + " - NOPE - ContinueShooting " + unit.id());
        return unit.isTargetInWeaponRangeAccordingToGame(unit.target());
//        return unit.hasWeaponRangeByGame(unit.targetUnitToAttack());
    }

    public Manager handle() {
        return usedManager(this);
    }
}
