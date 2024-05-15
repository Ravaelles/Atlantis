package atlantis.units.interrupt;

import atlantis.architecture.Manager;
import atlantis.decisions.Decision;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.util.We;
import bwapi.Color;

import static atlantis.units.interrupt.UnitAttackWaitFrames.waitedLongEnoughForAttackFrameToFinish;

public class ContinueShooting extends Manager {
    public ContinueShooting(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        if (We.terran()) return false;

        if (unit.isAttacking() && unit.lastActionLessThanAgo(2)) return true;
        if (!unit.isAction(Actions.ATTACK_UNIT)) return false;

        Decision decision;

        if (unit.isDragoon() && (decision = decisionForDragoon()).notIndifferent()) return decision.toBoolean();

        if (unit.isStartingAttack()) return true;
        if (unit.isAttackFrame()) return true;

        if (unit.lastAttackFrameLessThanAgo(5)) return false;
        if (!unit.hasValidTarget()) return false;

//        if (unit.lastActionMoreThanAgo(8, Actions.ATTACK_UNIT)) return false;
//        if (unit.lastActionMoreThanAgo(10, Actions.ATTACK_UNIT)) return false;
        if (unit.isMelee() && unit.lastActionMoreThanAgo(50)) return false;

        if (unit.lastAttackFrameMoreThanAgo(30 * 6)) return true;

//        if (unit.isStopped()) return false;
//        if (ShouldRetreat.shouldRetreat(unit)) return false;
//
//        if (unit.isDragoon() && doesNotApplyForDragoon()) return false;
//        if (unit.isMarine() && doesNotApplyForMarine()) return false;

        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return true;

        return false;

//        if (true) return false;
//
////        if (
////            unit.isDragoon()
////                && unit.isMissionDefendOrSparta()
////                && unit.meleeEnemiesNearCount(1.2) >= 1
////        ) return false;
//
//        if (unit.lastActionMoreThanAgo(15)) return false;
//
////        if (unit.isMissionSparta() && unit.isDragoon() && unit.distToTarget() > 4) return false;
//
////        if (unit.isStartingAttack()) return true;
////        if (unit.isAttackFrame()) return true;
//
//        if (!unit.hasValidTarget()) return false;
////        if (unit.lastActionMoreThanAgo()) return false;
//
//        if (UnitAttackWaitFrames.unitAlreadyStartedAttackAnimation(unit)) return true;
//
////        System.out.println("@ " + A.now() + " - NOPE - ContinueShooting " + unit.id());
//        return unit.isTargetInWeaponRangeAccordingToGame(unit.target());
////        return unit.hasWeaponRangeByGame(unit.targetUnitToAttack());
    }

    private Decision decisionForDragoon() {
//        System.err.println(A.now() + " // " + unit.lastAttackFrameMoreThanAgo(30 * 6));

//        if (
////            unit.cooldown() <= 5
////            unit.lastAttackFrameMoreThanAgo(30)
//            unit.lastActionLessThanAgo(30 * 5, Actions.ATTACK_UNIT)
//        ) return Decision.ALLOWED;

        int sa = unit.lastStartedAttackAgo();
        int af = unit.lastAttackFrameAgo();

//        System.err.println("sa:" + sa + ", la:" + unit.lastAttackFrameAgo());
        if (sa <= 40) {
            if (sa < UnitAttackWaitFrames.attackAnimationFrames(AUnitType.Protoss_Dragoon)) {
                return Decision.ALLOWED;
                //            System.err.println("Dragoon " + unit.id() + " - FORBIDDEN sa: (" + sa);
            }

            if (af >= 30 * 3 && unit.hasTarget() && unit.target().isHydralisk()) return Decision.ALLOWED;
            
            return Decision.FORBIDDEN;
//            return Decision.FORBIDDEN;
        }

        int maxFramesAgo = maxFramesAgoForDragoon();
        if (unit.lastActionLessThanAgo(maxFramesAgo, Actions.ATTACK_UNIT)) {
//            System.out.println("@" + A.fr + " ----> continue shooting");
            return Decision.ALLOWED;
        }

//        System.err.println("Dragoon " + unit.id() + " - NO ");

//        if (unit.isAttackFrame()) {
////            unit.paintCircleFilled(10, Color.Green);
////            System.out.println("A / " + unit.lastAttackFrameAgo() + " / " + unit.lastAttackOrderAgo());
//            if (unit.lastAttackFrameAgo() <= 1) return Decision.ALLOWED;
//        }

//        else if (unit.isStartingAttack()) {
//            unit.paintCircleFilled(10, Color.Teal);
//            System.out.println("B / " + unit.lastAttackFrameAgo() + " / " + unit.lastAttackOrderAgo());
//        }
//        if (unit.isAttackFrame()) return Decision.ALLOWED;

//        if (waitedLongEnoughForAttackFrameToFinish(unit)) return Decision.ALLOWED;

//        System.out.println("@" + A.fr + " _NO_");
        return Decision.FORBIDDEN;

//        if (unit.lastAttackFrameLessThanAgo(1)) return Decision.FORBIDDEN;
//
//        return (
//            unit.lastActionLessThanAgo(50 + (unit.woundHp() <= 30 ? 50 : 0), Actions.ATTACK_UNIT)
//                || (unit.hp() >= 21 && unit.lastAttackFrameMoreThanAgo(30 * 3))
//        ) ? Decision.ALLOWED : Decision.INDIFFERENT;
    }

    private int maxFramesAgoForDragoon() {
        if (unit.lastAttackFrameMoreThanAgo(30 * 9)) return 30 * 7;

        boolean longNoAttackFrame = unit.lastAttackFrameMoreThanAgo(60);

//        if (
//            longNoAttackFrame
////                && (unit.shieldDamageAtMost(10) || unit.isTargetInWeaponRangeAccordingToGame())
//                && unit.isTargetInWeaponRangeAccordingToGame()
//        ) return 150;

        if (longNoAttackFrame) {
            if (unit.isHealthy()) return 30 * 6;
            if (unit.isTargetInWeaponRangeAccordingToGame()) return 30 * 3;
            return 30 * 2;
        }

//        if (unit.isAttackFrame()) return 50;

//        System.out.println("... " + unit.isStartingAttack() + " / " + unit.isAttackFrame());
        return unit.isHealthy() ? 50 : 10;
    }

    private boolean doesNotApplyForDragoon() {
        double minDistToEnemy = 1.2 + unit.woundPercent() / 80.0;

        if (unit.hp() <= 30) return true;
        if (unit.meleeEnemiesNearCount(minDistToEnemy) >= 1) return true;

        return false;
    }

    private boolean doesNotApplyForMarine() {
        double minDistToEnemy = 1.5 + unit.woundPercent() / 80.0;

        if (unit.hp() <= 21) return true;
        if (!unit.hasMedicInRange() && unit.meleeEnemiesNearCount(minDistToEnemy) >= 1) return true;

        return false;
    }

    public Manager handle() {
        return this;
//        return usedManager(this);
    }
}
