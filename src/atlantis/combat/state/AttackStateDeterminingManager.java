package atlantis.combat.state;

import atlantis.architecture.Manager;
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

        return null;
    }

    private AttackState determineAttackState() {
        cooldown = unit.cooldown();
//        System.err.println(A.now + " ### cooldown = " + cooldown);

        if (cooldown == 0) {
            if (!unit.isAttacking()) return unit.setAttackState(AttackState.NONE);
            if (unit.hasValidTarget()) return unit.setAttackState(AttackState.TARGET_ACQUIRED);
        }

        if (unit.isStartingAttack()) {
//        if (unit.isStartingAttack() || unit.isAttackFrame()) {
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
//        System.err.println(A.now + ": unit.lastBulletAge() = " + bulletAge);

        if (bulletAge >= 0 && bulletAge <= 3) return true;

        return false;
    }

//    private boolean hasPendingBullet() {
//        return false;
//    }
}
