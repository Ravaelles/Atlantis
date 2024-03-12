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
        if (unit.isStopped()) return false;
        if (!unit.isAttacking()) return false;

        if (unit.isDragoon()) {
            if (doesNotApplyForDragoon()) return false;
        }

        if (unit.isStartingAttack()) return true;
        if (unit.isAttackFrame()) return true;

//        if (
//            unit.isDragoon()
//                && unit.isMissionDefendOrSparta()
//                && unit.meleeEnemiesNearCount(1.2) >= 1
//        ) return false;

        if (unit.lastActionMoreThanAgo(15)) return false;

//        if (unit.isMissionSparta() && unit.isDragoon() && unit.distToTarget() > 4) return false;

//        if (unit.isStartingAttack()) return true;
//        if (unit.isAttackFrame()) return true;

        if (!unit.hasValidTarget()) return false;
//        if (unit.lastActionMoreThanAgo()) return false;

        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return true;

//        System.out.println("@ " + A.now() + " - NOPE - ContinueShooting " + unit.id());
        return unit.isTargetInWeaponRangeAccordingToGame(unit.target());
//        return unit.hasWeaponRangeByGame(unit.targetUnitToAttack());
    }

    private boolean doesNotApplyForDragoon() {
        double minDistToEnemy = 1.2 + unit.woundPercent() / 80.0;

        if (unit.hp() <= 30) return false;
        if (unit.meleeEnemiesNearCount(minDistToEnemy) >= 1) return true;

        return false;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
