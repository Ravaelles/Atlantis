package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ContinueShotAnimation extends Manager {
    public ContinueShotAnimation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        System.err.println(A.now() + " isAttacking = " + unit.isAttacking() + " / " + unit.u().getOrder() + " / " + unit.u().getOrder().getClass().getSimpleName());
//        if (!unit.isTargetInWeaponRangeAccordingToGame()) return false;

        if (unit.isStopped()) return false;
        if (!unit.isAttacking()) return false;
        if (!unit.hasValidTarget()) return false;

        if (unit.isZealot()) {
            if (unit.isTargetRanged()) return true;
//            System.err.println("unit.cooldown() = " + unit.cooldown());
            
            if (unit.cooldown() >= 5 && unit.cooldown() <= 23) return false;

            if (
                unit.isAttacking()
                    && unit.hasValidTarget()
                    && unit.isTargetInWeaponRangeAccordingToGame()
            ) return true;
        }

        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return true;

        /*
         * Warning: Careful here as too quickly stopping attacking will cause Goon to freeze!
         */
        if (unit.isDragoon()) {
            if (unit.cooldown() >= 10 && unit.cooldown() <= 21) return false;

            if (
                unit.isAttacking()
                    && unit.hasValidTarget()
                    && unit.isTargetInWeaponRangeAccordingToGame()
            ) return true;
//            System.out.println("-------- QUIT with " + unit.cooldown());
//            unit.paintCircleFilled(10, Color.Red);
//            System.out.println(A.now() + " / " + unit.cooldown() + " / AF=" + unit.isAttackFrame());
        }

        if (unit.isStartingAttack()) return true;
//        System.out.println(A.now() + " / " + unit.cooldown() + " / AF=" + unit.isAttackFrame());
        if (unit.isAttackFrame()) return true;

//        System.err.println("---- unit.cooldown() = " + unit.cooldown());

//        if (unit.cooldown() >= 10) return false;

//        System.err.println("BBBB unit.cooldown() = " + unit.cooldown());

//        if (unit.meleeEnemiesNearCount(2.9) > 0) return false;

        if (unit.isActiveManager(this.getClass())) {
//            System.err.println("FA = " + unit.lastAttackFrameAgo());

            if (unit.lastAttackFrameMoreThanAgo(24)) {
                return true;
            }

//            System.err.println(unit.lastAttackFrameAgo());
            return unit.isTargetInWeaponRangeAccordingToGame();
        }

//        if (
//            unit.isActiveManager(AttackNearbyEnemies.class)
//                && (unit.lastAttackFrameMoreThanAgo(30) || unit.lastPositionChangedLessThanAgo(10))
//        ) return unit.isTargetInWeaponRangeAccordingToGame();

        return false;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
