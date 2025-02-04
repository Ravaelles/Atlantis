package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;

public class ContinueShotAnimation extends Manager {
    public ContinueShotAnimation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        System.err.println(A.now() + " isAttacking = " + unit.isAttacking() + " / " + unit.u().getOrder() + " / " + unit.u().getOrder().getClass().getSimpleName());
//        if (!unit.isTargetInWeaponRangeAccordingToGame()) return false;

//        if (unit.isStopped()) return false;
        if (!unit.isAttacking()) return false;
        if (!unit.hasValidTarget()) return false;
//        if (unit.isRunning()) return false;
//        if (unit.hp() <= 40) return false;
//        if (unit.shotSecondsAgo() >= 5) return false;
//        if (unit.lastActionMoreThanAgo(15) && !unit.isTargetInWeaponRangeAccordingToGame()) return false;
        if (!unit.isTargetInWeaponRangeAccordingToGame()) {
            if (unit.lastActionLessThanAgo(20, Actions.MOVE_FORMATION)) return false;
            if (unit.shotSecondsAgo() >= 4 && unit.lastActionMoreThanAgo(8)) return false;

            if (unit.lastActionLessThanAgo(45)) return true;
        }

        // =========================================================

        Decision decision;

        if ((decision = forDragoon()).notIndifferent()) return decision.toBoolean();
        if ((decision = forZealot()).notIndifferent()) return decision.toBoolean();

        if ((decision = forMarine()).notIndifferent()) return decision.toBoolean();

        // =========================================================

        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return true;

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

    private Decision forDragoon() {
        /*
         * Warning: Careful here as too quickly stopping attacking will cause Goon to freeze!
         */
        if (!unit.isDragoon()) return Decision.INDIFFERENT;

        if (!unit.attackState().finishedShooting()) return Decision.TRUE;
        else return Decision.FALSE;

//        if (
//            unit.lastAttackFrameLessThanAgo(20)
//                && unit.cooldown() >= 10
//                && unit.cooldown() <= UnitAttackWaitFrames.DRAGOON
//        ) {
////            System.out.println(A.now() + " DONT");
//            return Decision.FALSE;
//        }
//
//        if (unit.lastAttackFrameMoreThanAgo(10)) {
//            return Decision.TRUE;
//        }
//
//        if (
//            unit.isAttacking()
//                && unit.hasValidTarget()
//                && unit.distToTargetLessThan(OurDragoonRange.range() + 0.5)
////                    && unit.isTargetInWeaponRangeAccordingToGame()
////                    && unit.shotSecondsAgo() >= 1.01
////                && unit.eval() >= 0.7
//        ) {
////            System.out.println(A.now() + " OK");
////            System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - ");
//            return Decision.TRUE;
//        }
////            System.out.println("-------- QUIT with " + unit.cooldown());
////            unit.paintCircleFilled(10, Color.Red);
////            System.out.println(A.now() + " / " + unit.cooldown() + " / AF=" + unit.isAttackFrame());
//
//        return Decision.INDIFFERENT;
    }

    private Decision forMarine() {
        if (!unit.isMarine()) return Decision.INDIFFERENT;

        if (true) return Decision.FALSE;
        if (unit.cooldown() >= 14) {
            System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.cooldown());
            return Decision.FALSE;
        }

        if (
            unit.isAttacking()
                && unit.lastAttackFrameMoreThanAgo(20)
                && unit.lastActionLessThanAgo(30, Actions.ATTACK_UNIT)
//                && !unit.isTarget(Zerg_Lurker)
                && unit.hasValidTarget()
                && unit.isTargetInWeaponRangeAccordingToGame()
        ) {
//            unit.paintCircleFilled(10, Color.Red);
            return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    private Decision forZealot() {
        if (unit.isZealot()) {
            if (unit.isTargetRanged()) return Decision.TRUE;
//            System.err.println("unit.cooldown() = " + unit.cooldown());

            if (unit.cooldown() >= 5 && unit.cooldown() <= 22) return Decision.FALSE;

            if (
                unit.isAttacking()
                    && unit.hasValidTarget()
                    && unit.distTo(unit.target()) <= 1.2
            ) return Decision.TRUE;
        }

        return Decision.INDIFFERENT;
    }

    public Manager handle() {
        return usedManager(this);
    }
}
