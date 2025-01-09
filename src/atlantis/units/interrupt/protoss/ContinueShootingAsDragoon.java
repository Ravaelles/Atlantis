package atlantis.units.interrupt.protoss;

import atlantis.decisions.Decision;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.interrupt.UnitAttackWaitFrames;

//public class ContinueShootingAsDragoon {
//    private static boolean targetInWeaponRange;
//
//    public static Decision check(AUnit unit) {
//        if (!unit.isDragoon()) return Decision.INDIFFERENT;
//
//        int sa = unit.lastStartedAttackAgo();
//        int af = unit.lastAttackFrameAgo();
//
//        // =========================================================
//
//        targetInWeaponRange = unit.isTargetInWeaponRangeAccordingToGame();
//        if (!targetInWeaponRange) return Decision.FORBIDDEN;
//
//        if (af >= 30 * 3) return Decision.ALLOWED;
//
//        // =========================================================
//
/// /        System.err.println("sa:" + sa + ", la:" + unit.lastAttackFrameAgo());
//        if (sa <= 40) {
//            if (sa < UnitAttackWaitFrames.attackAnimationFrames(AUnitType.Protoss_Dragoon)) {
//                return Decision.ALLOWED;
//                //            System.err.println("Dragoon " + unit.id() + " - FORBIDDEN sa: (" + sa);
//            }
//
//            if (af >= 30 * 3 && unit.hasTarget() && unit.target().isHydralisk()) return Decision.ALLOWED;
//
//            return Decision.FORBIDDEN;
//        }
//
//        // =========================================================
//
//        boolean hasRangedEnemies = unit.hasRangedEnemies(3);
//        if (!hasRangedEnemies && af >= 30 * 2) return Decision.ALLOWED;
//
//        // =========================================================
//
//        if (
////            unit.lastAttackFrameMoreThanAgo(30)
//            unit.cooldown() <= 5
//                && unit.hasTarget()
//                && unit.target().isHydralisk()
//                && unit.lastActionLessThanAgo(30 * 5, Actions.ATTACK_UNIT)
//                && targetInWeaponRange
//        ) return Decision.ALLOWED;
//
//        // =========================================================
//
//        int maxFramesAgo = maxFramesAgoForDragoon(unit);
//        if (unit.lastActionLessThanAgo(maxFramesAgo, Actions.ATTACK_UNIT)) {
////            System.out.println("@" + A.fr + " ----> continue shooting");
//            return Decision.ALLOWED;
//        }
//
//        return Decision.FORBIDDEN;
//    }
//
//    private static int maxFramesAgoForDragoon(AUnit unit) {
//        if (unit.lastAttackFrameMoreThanAgo(30 * 9)) return 30 * 7;
//
//        boolean longNoAttackFrame = unit.lastAttackFrameMoreThanAgo(60);
//
////        if (
////            longNoAttackFrame
//////                && (unit.shieldDamageAtMost(10) || unit.isTargetInWeaponRangeAccordingToGame())
////                && unit.isTargetInWeaponRangeAccordingToGame()
////        ) return 150;
//
//        if (longNoAttackFrame) {
//            if (unit.isHealthy()) return 30 * 6;
//            if (unit.isTargetInWeaponRangeAccordingToGame()) return 30 * 3;
//            return 30 * 2;
//        }
//
////        if (unit.isAttackFrame()) return 50;
//
////        System.out.println("... " + unit.isStartingAttack() + " / " + unit.isAttackFrame());
//        return unit.isHealthy() ? 50 : 10;
//    }
//
////    private boolean doesNotApplyForDragoon() {
////        double minDistToEnemy = 1.2 + unit.woundPercent() / 80.0;
////
////        if (unit.hp() <= 30) return true;
////        if (unit.meleeEnemiesNearCount(minDistToEnemy) >= 1) return true;
////
////        return false;
////    }
//}
