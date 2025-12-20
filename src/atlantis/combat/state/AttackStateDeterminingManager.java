package atlantis.combat.state;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.interrupt.UnitAttackWaitFrames;

public class AttackStateDeterminingManager extends Manager {
    private int cooldown;

    public AttackStateDeterminingManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isOur() && unit.hasAnyWeapon();
    }

    @Override
    public Manager handle() {
        determineAttackState();

//        System.out.println(A.now + ": " + unit.attackState());

        return null;
    }

    private AttackState determineAttackState() {
        cooldown = unit.cooldown();

        if (cooldown == 0) {
            if (!unit.isAttacking() && !unit.isHoldingToShoot()) return unit.setAttackState(AttackState.NONE);
            if (unit.hasValidTarget()) return unit.setAttackState(AttackState.TARGET_ACQUIRED);
        }

        if (unit.isStartingAttack()) {
            if (!unit.attackState().pending()) return unit.setAttackState(AttackState.STARTING);
        }

        if (unit.type().createsBullets()) {
            if (createdReadyBullet()) return unit.setAttackState(AttackState.FINISHED);
            return null;
        }
        else {
            if (!UnitAttackWaitFrames.waitedLongEnoughForStartedAttack(unit)) {
                return unit.setAttackState(AttackState.PENDING);
            }
        }

        return unit.setAttackState(AttackState.FINISHED);
    }

    private boolean createdReadyBullet() {
        int bulletAge = unit.lastBulletAge();

        if (bulletAge >= 1 && bulletAge <= 2) {
//            System.err.println("bulletAge = " + bulletAge);
            return true;
        }
//        if (bulletAge >= 3 && bulletAge <= 4) return true;

        return false;
    }

//    private boolean hasPendingBullet() {
//        return false;
//    }
}
