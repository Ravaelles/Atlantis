package atlantis.combat.micro.dancing.hold;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossAttackHoldToShoot {
    public static boolean holdInsteadAttack(AUnit unit, AUnit target) {
        if (target == null) target = unit.lastTarget();
        if (target == null || target.hp() <= 0) return f("noValidTarget");

        if (target.isRanged()) return false;
        if (unit.isHealthy()) return false;

        if (!We.protoss()) return false;
        if (!unit.isRanged()) return false;
        if (unit.cooldown() >= 11) return f("cooldownTooHigh");
        if (unit.attackState().startingOrPending()) return f("attackStatePending");

        if (unit.isHoldingToShoot() && unit.lastActionLessThanAgo(4, Actions.HOLD_TO_SHOOT)) {
            return t("justHeldToShoot");
        }
        if (
            !target.isMoving() && unit.lastUnderAttackMoreThanAgo(50) && A.now >= 20
        ) return f("targetNotMoving");

        if (
            unit.cooldown() <= 8 && unit.isTargetInWeaponRangeAccordingToGame(target)
        ) return f("alreadyInRange");

        if (unit.isHoldingToShoot()) {
            if (unit.speed() >= 0.9) return true;
//            System.err.println(A.now() + " ## ContinueHolding, speed:" + unit.speed());
            if (unit.lastActionLessThanAgo(15, Actions.HOLD_TO_SHOOT)) return true;
        }

        if (Enemy.terran() && unit.enemiesNear().tanks().notEmpty()) return f("enemyTankNearby");
        if (unit.lastPositionChangedAgo() >= 60) return f("positionStale");
        if (unit.isAttacking() && unit.lastActionLessThanAgo(20, Actions.HOLD_TO_SHOOT)) return f("recentAttackAction");
        if (target.hasBiggerWeaponRangeThan(unit)) return f("targetHasBiggerRange");
        if (!unit.isHoldingPosition() && unit.lastActionLessThanAgo(10, Actions.HOLD_TO_SHOOT)) return f("notHoldingRecently");
        if (unit.distTo(target) <= unit.weaponRangeAgainst(target) + 0.07) return f("alreadyCloseEnough");
        if (
            !unit.isOtherUnitFacingThisUnit(target)
                && unit.rangedEnemiesCount(1.3) <= (target.isRanged() ? 1 : 0)
        ) return f("targetNotFacingUs");

        // ===

//        if (unit.isHoldingToShoot()) return f("");

        double dist, ourRange, theirRange;
        if (
            //                && !unit.isTargetInWeaponRangeAccordingToGame(target)
//            (dist = distToTargetWithFactors(unit, target)) >= minDist(unit, target)
            (dist = distToTargetWithFactors(unit, target)) > ((ourRange = unit.weaponRangeAgainst(target)))
//                && unit.hp() >= 23
//                && dist < (2 + (ourRange = unit.weaponRangeAgainst(target)))
//                && ourRange >= (theirRange = target.weaponRangeAgainst(unit))
                && (
                            (target.isMoving() || unit.isHoldingToShoot())
//                    !unit.isTargetInWeaponRangeAccordingToGame(target)
//                        || (
//                            (target.isMoving() || unit.isHoldingToShoot())
//                                && unit.distTo(target) > unit.weaponRangeAgainst(target)
//                    )
            )
//                    && (new HoldToShoot(unit)).forceHandle() != null
        ) {
//            System.out.println("@" + A.now + " - " + unit.distToDigit(target) + " dist, HOLD !!!!!");
//            if (!unit.isHoldingPosition()) {
            unit.holdPosition(Actions.HOLD_TO_SHOOT, "HoldToShoot");
//            } else {
//                unit.stop("StopToShoot");
//            }

            return true;
        }

        return f("GenericNoHold");
    }

    private static boolean f(String reasonWhyNot) {
//        System.err.println("@" + A.now + " - Don't hold: " + reasonWhyNot);
        return false;
    }

    private static boolean t(String reasonWhy) {
//        System.err.println("@" + A.now + " - HoldToShoot: " + reasonWhy);
        return true;
    }

    private static double distToTargetWithFactors(AUnit unit, AUnit target) {
        double unitSpeedBonus = unit.speed() / 10;
        double targetMovingBonus = targetMovingDistBonus(target);
        double lowHealthBonus = unit.hp() <= 41 ? 0.1 : 0;
//        System.err.println("unit bonus = " + unitSpeedBonus + " / " + unit.speed());
//        System.err.println("target bonus = " + targetMovingBonus + " / " + target.speed());

        return unit.distTo(target)
            + 0.3
            + lowHealthBonus
            + unitSpeedBonus
            + targetMovingBonus;
    }

    private static double targetMovingDistBonus(AUnit target) {
        if (target.isZerg()) return target.speed() / 7;
        if (target.isProtoss()) return target.speed() / 10;

        return target.speed() / 8;
    }

//    private static double minDist(AUnit unit, AUnit target) {
////        return unit.shieldWound() <= 30 ? 3.4 : 3.8;
//        return OurDragoonRange.range() + 0.05;
//    }
}
