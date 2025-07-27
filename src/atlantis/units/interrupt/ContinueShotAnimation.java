package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.util.We;

import static atlantis.units.actions.Actions.HOLD_TO_SHOOT;

public class ContinueShotAnimation extends Manager {
    public ContinueShotAnimation(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isOur()) return false;

//        System.err.println(A.now() + " isAttacking = " + unit.isAttacking() + " / " + unit.u().getOrder() + " / " + unit.u().getOrder().getClass().getSimpleName());
//        if (!unit.isTargetInWeaponRangeAccordingToGame()) return false;

        boolean isHoldingToShoot = unit.isAction(HOLD_TO_SHOOT);
        if (!unit.isAttacking() && !isHoldingToShoot) return false;
        if (!unit.hasValidTarget()) return false;
//        if (unit.isRunning() || unit.isRetreating()) return false;
//        if (unit.lastStartedRetreatingAgo() <= 60) return false;

//        if (!isHoldingToShoot && !unit.isTargetInWeaponRangeAccordingToGame()) {
//        if (!isHoldingToShoot) {
//            return false;
//        }

        if (!unit.shotSecondsAgo(3) && !unit.isTargetInWeaponRangeAccordingToGame()) {
            return false;
        }

        if (!unit.attackState().finishedShooting()) {
//            System.err.println(A.minSec() + " - " + unit.typeWithUnitId() + " - shooting");
            return true;
        }

//        if (!unit.isTargetInWeaponRangeAccordingToGame()) {
//            if (unit.distToTarget() > 7) return false;
//            if (unit.isDragoon() && unit.hp() <= 60) return false;
//            if (unit.lastActionLessThanAgo(20, Actions.MOVE_FORMATION)) return false;
//            if (unit.shotSecondsAgo() >= 4 && unit.lastActionMoreThanAgo(8)) return false;
//
////            if (A.s >= 5 && unit.lastActionLessThanAgo(45)) return true;
//        }

        // =========================================================

        Decision decision;

        if (We.protoss()) {
            if ((decision = forDragoon()).notIndifferent()) return decision.toBoolean();
            if ((decision = forZealot()).notIndifferent()) return decision.toBoolean();
        }

        if (We.terran()) {
            if ((decision = forMarine()).notIndifferent()) return decision.toBoolean();
        }

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
    }

    private Decision forMarine() {
        if (!unit.isMarine()) return Decision.INDIFFERENT;

//        if (true) return Decision.FALSE;
//        System.out.println(A.now + " - cooldown: " + unit.cooldown() + " / " + unit.cooldownAbsolute());

        if (unit.cooldown() >= 17) {
//            System.err.println("@ " + A.now() + " - DONT - " + unit.typeWithUnitId() + " - " + unit.cooldown());
            return Decision.FALSE;
        }

        if (true) return Decision.FALSE;

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
